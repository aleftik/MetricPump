package com.appdynamics.metricpump;

import com.appdynamics.metricpump.api.Reader;
import com.appdynamics.metricpump.appdynamics.AppDynamicsControllerEndpoint;
import com.appdynamics.metricpump.appdynamics.model.*;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;


import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MetricsReader implements Reader {
    public static final int MAX_METRICS = 5000;
    private static Logger logger = Logger.getLogger(MetricsReader.class.getName());
    private static int CACHE_TIMEOUT = 60*1000*15;
    private boolean continueRunning = true;
    private AppDynamicsControllerEndpoint endpoint;
    private BlockingQueue<MetricUploadRequest> queue;
    private PassiveExpiringMap apps = new PassiveExpiringMap(CACHE_TIMEOUT);
    private PassiveExpiringMap tierCache = new PassiveExpiringMap(CACHE_TIMEOUT);
    private PassiveExpiringMap btCache = new PassiveExpiringMap(CACHE_TIMEOUT);
    private PassiveExpiringMap backendCache = new PassiveExpiringMap(CACHE_TIMEOUT);
    private PassiveExpiringMap appdTierCache = new PassiveExpiringMap(CACHE_TIMEOUT);
    private int readerCount = 5;
    private int readInterval = 2;

    public MetricsReader(AppDynamicsControllerEndpoint endpoint, BlockingQueue<MetricUploadRequest> queue, int readerCount) {
        this.endpoint = endpoint;
        this.queue = queue;
        this.readerCount = readerCount;
    }

    public synchronized void read() {
        logger.log(Level.FINER, "Begin reading applications");
        List<Application> apps  = getApplications();
        logger.log(Level.FINER, "End reading applications");
        ExecutorService pool = Executors.newFixedThreadPool(readerCount);
        List <Future> readers = new ArrayList<Future>();
        for (Application app:apps) {
            readers.add(pool.submit(new Runnable() {
                @Override
                public void run() {
                    handleApplication(app);
                }
            }));
        }
        while(isRunning(readers)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public boolean isRunning(List<Future> readers) {
        for (Future f: readers) {
            if (!f.isDone()) {
                return false;
            }
        }
        return true;
    }

    private void handleApplication(Application app) {
        logger.log(Level.FINER,"Begin reading for app:" + app.getName());
        MetricUploadRequest request = new MetricUploadRequest();
        request.setApplication(app);
        logger.log(Level.FINER,"Begin reading backends for app:" + app.getName());
        List<AppDynamicsBackend> backends = readBackends(app);
        request.setBackends(backends);
        logger.log(Level.FINER,"Done reading backends for app:" + app.getName());
        logger.log(Level.FINER,"Begin reading tiers for app:" + app.getName());
        List<Tier> tiers = readTiers(app);
        logger.log(Level.FINER,"Done reading tiers for app:" + app.getName());
        for (Tier tier: tiers) {
            List<AppDynamicsMetric> metrics = readNetworkMetrics(app,tier);
            request.addMetrics(metrics);
        }

        List<AppDynamicsMetric> backendMetrics = readBackendMetrics(app.getName());
        request.addMetrics(backendMetrics);

        List<AppDynamicsMetric> customMetrics = readCustomMetrics(app.getName());
        request.setApplication(app);
        request.addMetrics(customMetrics);

        logger.log(Level.FINER,"Begin reading bts for app:" + app.getName());
        List<BusinessTransaction> bts  = readBTsForApp(app);
        logger.log(Level.FINER,"Done reading bts for app:" + app.getName());
        request.setBts(bts);
        for (BusinessTransaction bt: bts) {
            request.setTier(findTier(bt,app));
            List<AppDynamicsMetric> metrics = readBTMetrics(app.getName(), bt);
            request.addMetrics(metrics);
        }
        try {
            logger.log(Level.FINER,"Done reading info for app" + app.getName()  + " queueing");
            logger.log(Level.FINEST,"Size of request is "  + request.getMetrics().size());
            while (request.getMetrics().size() > MAX_METRICS) {
                logger.log(Level.FINER,"Spliting request into chunks");
                MetricUploadRequest splitRequest =  splitCollection(request);
                queue.put(splitRequest);
            }
            queue.put(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private MetricUploadRequest splitCollection(MetricUploadRequest request) {
        int count = request.getMetrics().size();
        int amountToSplit = count;
        AppDynamicsMetric [] metrics = new AppDynamicsMetric[count];
        metrics = request.getMetrics().toArray(metrics);
        if (count >= MAX_METRICS) {
            amountToSplit = MAX_METRICS;
        }
        AppDynamicsMetric [] destMetrics = new AppDynamicsMetric[amountToSplit];
        System.arraycopy(metrics,0,destMetrics,0,amountToSplit);
        List metricsToRemove = new ArrayList(Arrays.asList(destMetrics));
        List originalList = request.getMetrics();
        originalList.removeAll(metricsToRemove);
        request.setMetrics(originalList);
        MetricUploadRequest splitRequest = new MetricUploadRequest();
        splitRequest.setMetrics(metricsToRemove);
        splitRequest.setApplication(request.getApplication());
        splitRequest.setBackends(request.getBackends());
        splitRequest.setBts(request.getBts());
        splitRequest.setTier(request.getTier());
        splitRequest.setTiers(request.getTiers());
        return  splitRequest;
    }


    private List<AppDynamicsMetric> readCustomMetrics(String appname) {
        String metricPath = metricPathForCustomMetrics(appname);
        List<AppDynamicsMetric> metrics = new ArrayList<AppDynamicsMetric>();
        Response response = null;
        Client client = RESTClientFactory.create(endpoint);
        try {
             response = client.target(this.endpoint.getHost() + metricPath).request().get();
            if (response.getStatus() == 200) {
                metrics = response.readEntity(new GenericType<List<AppDynamicsMetric>>() {
                });
                return metrics;
            } else {
                logger.severe(response.getStatus() + " from metric read");
            }

        } catch (Exception ex) {
            logger.log(Level.SEVERE,"unable to retrieve custom metrics",ex);
        } finally {
            close(response);
        }
        return null;

    }

    private String metricPathForCustomMetrics(String appname) {
        String path = "/controller/rest/applications/"+ URLEncoder.encode(appname)+ "/metric-data?metric-path=Application%20Infrastructure%20Performance%7CApp%20Server%7CCustom%20Metrics%7C%2A%7C%2A%7C%2A%7C%2A&time-range-type=BEFORE_NOW&duration-in-mins="+readInterval;
        return path;
    }

    private List<AppDynamicsMetric> readNetworkMetrics(Application app,Tier tier) {
        String metricPath = metricPathForNetwork(app.getName(),tier.getName());
        List<AppDynamicsMetric> metrics = new ArrayList<AppDynamicsMetric>();
        Client client = RESTClientFactory.create(endpoint);
        Response response = null;
        try {
             response = client.target(this.endpoint.getHost() + metricPath).request().get();
            if (response.getStatus() == 200) {
                metrics = response.readEntity(new GenericType<List<AppDynamicsMetric>>() {
                });
                return metrics;
            } else if (response.getStatus() == 401) {
                logger.severe("401 from metric read");
                Set features = client.getConfiguration().getInstances();
                Iterator it = features.iterator();
                while (((Iterator) it).hasNext()) {
                    HttpAuthenticationFeature auth = (HttpAuthenticationFeature) it.next();
                    logger.severe(auth.toString());
                }
            }

        } catch (Exception ex) {
            logger.log(Level.SEVERE,"unable to retrieve backend metrics",ex);
        } finally {
            close(response);
        }
        return null;
    }

    private String metricPathForNetwork(String appName, String tierName) {
        String path = "/controller/rest/applications/" + URLEncoder.encode(appName) + "/metric-data?metric-path=" + "Application%20Infrastructure%20Performance%7C" + URLEncoder.encode(tierName) +   "%7CAdvanced%20Network%7CFlows%7C*%7C*" + "&time-range-type=BEFORE_NOW&duration-in-mins=" + readInterval;
        return path;
    }

    private List<Tier> readTiers(Application app) {
        List<Tier> tiers = (List<Tier>)tierCache.get(app.getId());
        Response response = null;
        if (tiers == null) {
            Client client = RESTClientFactory.create(endpoint);
            tiers = new ArrayList<>();
            try {
                 response = client.target(this.endpoint.getHost() + "/controller/rest/applications/" + app.getId() + "/tiers").request().get();
                if (response.getStatus() == 200) {
                    tiers = response.readEntity(new GenericType<List<Tier>>() {
                    });
                    updateTier(app,tiers);
                    tierCache.put(app.getId(),tiers);
                } else if (response.getStatus() == 401) {
                    logger.severe("401 from metric read");
                    Set features = client.getConfiguration().getInstances();
                    Iterator it = features.iterator();
                    while (((Iterator) it).hasNext()) {
                        HttpAuthenticationFeature auth = (HttpAuthenticationFeature) it.next();
                        logger.severe(auth.toString());
                    }
                }

            } catch (Exception ex) {
                logger.log(Level.SEVERE, "unable to retrieve tiers", ex);
            } finally {
                close(response);
            }
        }
        return tiers;
}

    private List<AppDynamicsBackend> readBackends(Application app) {
        List<AppDynamicsBackend> backends = (List<AppDynamicsBackend>) backendCache.get(app.getId());
        Response response = null;
        if (backends == null) {
            Client client = RESTClientFactory.create(endpoint);
            backends = new ArrayList<AppDynamicsBackend>();
            try {
                 response = client.target(this.endpoint.getHost() + "/controller/rest/applications/" + app.getId() + "/backends").request().get();
                if (response.getStatus() == 200) {
                    backends = response.readEntity(new GenericType<List<AppDynamicsBackend>>() {
                    });
                    updateBackend(app, backends);
                    backendCache.put(app.getId(), backends);
                } else if (response.getStatus() == 401) {
                    logger.severe("401 from metric read");
                    Set features = client.getConfiguration().getInstances();
                    Iterator it = features.iterator();
                    while (((Iterator) it).hasNext()) {
                        HttpAuthenticationFeature auth = (HttpAuthenticationFeature) it.next();
                        logger.severe(auth.toString());
                    }
                }

            } catch (Exception ex) {
                logger.log(Level.SEVERE, "unable to retrieve backends", ex);
            } finally {
                close(response);
            }
        }
        return backends;
    }

    private void updateBackend(Application app, List<AppDynamicsBackend> backends) {
        Long id = app.getId();
        for (AppDynamicsBackend backend:backends) {
            backend.setAppId(id);
        }
    }


    private void updateBT(Application app, List<BusinessTransaction> bts) {
        Long id = app.getId();
        for (BusinessTransaction bt:bts) {
            bt.setAppId(id);
        }
    }


    private void updateTier(Application app, List<Tier> tiers) {
        Long id = app.getId();
        for (Tier tier:tiers) {
            tier.setAppId(id);
        }
    }

    private List<AppDynamicsMetric>  readBackendMetrics(String appName) {
        String metricPath = getPathForBackends(appName);
        List<AppDynamicsMetric> metrics = new ArrayList<AppDynamicsMetric>();
        Client client = RESTClientFactory.create(endpoint);
        Response response = null;
        try {
             response = client.target(this.endpoint.getHost() + metricPath).request().get();
            if (response.getStatus() == 200) {
                metrics = response.readEntity(new GenericType<List<AppDynamicsMetric>>() {
                });
            } else if (response.getStatus() == 401) {
                logger.severe("401 from metric read");
                Set features = client.getConfiguration().getInstances();
                Iterator it = features.iterator();
                while (((Iterator) it).hasNext()) {
                    HttpAuthenticationFeature auth = (HttpAuthenticationFeature) it.next();
                    logger.severe(auth.toString());
                }
            }

        } catch (Exception ex) {
            logger.log(Level.SEVERE,"unable to retrieve backend metrics",ex);
        } finally {
            close(response);
        }
        return metrics;

    }

    private List<AppDynamicsMetric>  readBTMetrics(String appName, BusinessTransaction bt) {
        String metricPath = getPathForBT(appName, bt);
        List<AppDynamicsMetric> metrics = new ArrayList<AppDynamicsMetric>();
        Client client = RESTClientFactory.create(endpoint);
        Response response = null;
        try {
             response = client.target(this.endpoint.getHost() + metricPath).request().get();
            if (response.getStatus() == 200) {
                metrics = response.readEntity(new GenericType<List<AppDynamicsMetric>>() {
                });
                return metrics;
            } else if (response.getStatus() == 401) {
                logger.severe("401 from metric read");
                Set features = client.getConfiguration().getInstances();
                Iterator it = features.iterator();
                while (((Iterator) it).hasNext()) {
                    HttpAuthenticationFeature auth = (HttpAuthenticationFeature) it.next();
                    logger.severe(auth.toString());
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE,"unable to retrieve bts",ex);
        } finally {
            close(response);
        }
        return null;
        
    }

    private String getPathForBackends(String appName) {
        return "/controller/rest/applications/" + URLEncoder.encode(appName) + "/metric-data?metric-path=Backends%7C*%7C*&time-range-type=BEFORE_NOW&duration-in-mins=" + readInterval;
    }

    public AppDynamicsTier findTier(BusinessTransaction bt, Application app) {
        Response response = null;
        String id  = bt.getId() + "-" + app.getId();
        AppDynamicsTier tier = (AppDynamicsTier) appdTierCache.get(id);
        if (tier == null) {
            Client client = RESTClientFactory.create(endpoint);
            try {
                response = client.target(this.endpoint.getHost() + "/controller/rest/applications/" + app.getId() + "/tiers/" + bt.getTierId() + "/nodes").request().get();
                if (response.getStatus() == 200) {
                    tier = response.readEntity(AppDynamicsTier.class);
                    tier.setTierName(bt.getTierName());
                    tier.setTierId(bt.getTierId());
                    appdTierCache.put(id,tier);
                } else if (response.getStatus() == 401) {
                    logger.severe("401 from metric read");
                    Set features = client.getConfiguration().getInstances();
                    Iterator it = features.iterator();
                    while (((Iterator) it).hasNext()) {
                        HttpAuthenticationFeature auth = (HttpAuthenticationFeature) it.next();
                        logger.severe(auth.toString());
                    }
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "unable to retrieve bts", ex);
            } finally {
                close(response);
            }
        }
        return tier;

    }

    private String getPathForBT(String appName, BusinessTransaction bt) {
        ///controller/rest/applications/ECommerce_E2E/metric-data?metric-path=Overall%20Application%20Performance%7CAverage%20Response%20Time%20%28ms%29\&time-range-type=BEFORE_NOW\&duration-in-mins=15
        String path = "/controller/rest/applications/" + URLEncoder.encode(appName) + "/metric-data?metric-path=" + "Business%20Transaction%20Performance%7CBusiness%20Transactions%7C" + URLEncoder.encode(bt.getTierName()) +   "%7C" + URLEncoder.encode(bt.getName())
                +"%7C*&time-range-type=BEFORE_NOW&duration-in-mins=" + readInterval;
        return path;
    }


    private List<BusinessTransaction> readBTsForApp(Application app) {
        List<BusinessTransaction> bts = (List<BusinessTransaction>) btCache.get(app.getId());
        Response response = null;
        if (bts == null) {
            bts = new ArrayList<BusinessTransaction>();
            Client client = RESTClientFactory.create(endpoint);
            try {
                 response = client.target(this.endpoint.getHost() + "/controller/rest/applications/" + app.getId() + "/business-transactions").request().get();
                if (response.getStatus() == 200) {
                    bts = response.readEntity(new GenericType<List<BusinessTransaction>>() {
                    });
                    updateBT(app,bts);
                    btCache.put(app.getId(), bts);

                }
                else if (response.getStatus() == 401) {
                    logger.severe("401 from metric read");
                    Set features = client.getConfiguration().getInstances();
                    Iterator it = features.iterator();
                    while (((Iterator) it).hasNext()) {
                        HttpAuthenticationFeature auth = (HttpAuthenticationFeature) it.next();
                        logger.severe(auth.toString());
                    }
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "unable to retrieve bts", ex);
            } finally {
                close(response);
            }
        }
        return bts;
    }

    private List<Application> getApplications() {
        List<Application> appList  = (List<Application>) this.apps.get("apps");
        Response response = null;
        if (appList == null) {
            appList = new ArrayList<Application>();
            Client client = RESTClientFactory.create(endpoint);
            try {
                response = client.target(this.endpoint.getHost() + "/controller/rest/applications").request().get();
                if (response.getStatus() == 200) {
                    appList = response.readEntity(new GenericType<List<Application>>() {
                    });
                    this.apps.put("apps",appList);
                } else if (response.getStatus() == 401) {
                    logger.severe("401 from metric read");
                    Set features = client.getConfiguration().getInstances();
                    Iterator it = features.iterator();
                    while (((Iterator) it).hasNext()) {
                        HttpAuthenticationFeature auth = (HttpAuthenticationFeature) it.next();
                        logger.severe(auth.toString());
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                logger.log(Level.SEVERE, "unable to retrieve applications", ex);
            } finally {
                close(response);
            }
        }
        return appList;
    }

    protected void close(Response response) {
        if (response != null) {
            try {
                response.close();
            } catch (Exception ex) {
                logger.severe(ex.toString());
            }
        }
    }
}
