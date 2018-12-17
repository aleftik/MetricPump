package com.appdynamics.metricpump;

import com.appdynamics.metricpump.appdynamics.model.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MetricWriterUtilsV2 {
    private static  final int MAX_LENGTH = 80;
    private static final Logger logger = Logger.getLogger(Logger.class.getName());

    public static String getMetricName(AppDynamicsMetric metric, boolean sanitize) {
        String metricPath = metric.getMetricPath();
        metricPath = StringUtils.remove(metricPath,"(ms)").trim();
        if (metric.isBackendMetric()) {
            return getMetricNameForBackend(metricPath);
        } else if (metric.isBusinessTransactionMetric()) {
            return getMetricNameForBT(metricPath);
        } else if (metric.isNetworkVizMetric()) {
            return getMetricNameForNetViz(metricPath);
        } else if (metric.isCustomMetric()) {
            return getMetricNameForCustom(metricPath);
        }
        if (sanitize) {
            metricPath = sanitize(metricPath);
        }
        return metricPath;
    }


    public static String getHost(AppDynamicsMetric metric) {
        return "";
    }

    public static JsonArray getTags(AppDynamicsMetric metric, MetricUploadRequest request, boolean sanitize) {
        JsonArray tags = new JsonArray();
        String application = request.getApplication().getName();
        if (sanitize) {
                application = sanitize(application);
        }

        tags.add("application:" + application);
        if (metric.isBusinessTransactionMetric()) {
            addTagsForBT(tags,metric, request, sanitize);
        }

        if (metric.isBackendMetric()) {
           addTagsForBackend(tags,metric, request, sanitize);
        }

        if (metric.isNetworkVizMetric()) {
            addTagsForFlows(tags,metric,request, sanitize);
        }

        return tags;
    }

    private static JsonArray addTagsForBackend(JsonArray tags,AppDynamicsMetric metric, MetricUploadRequest request,boolean sanitize) {
        Long id = request.getApplication().getId();
        String metricPath = metric.getMetricPath();
        String backendName = null;
        Map<String, AppDynamicsBackend> backends = request.getBackendMap();
        String application = request.getApplication().getName();
        if (metricPath.startsWith("Backends|Discovered backend call - ")) {
            metricPath = StringUtils.remove(metric.getMetricPath(), "Backends|Discovered backend call - ");
            backendName = StringUtils.left(metricPath,metricPath.indexOf('|'));
            if (sanitize) {
                backendName = sanitize(backendName);
                application = sanitize(application);
                metricPath = sanitize(metricPath);
            }
            tags.add("backend:"+ metricPath);
        } else {
            metricPath = StringUtils.remove(metricPath,"Backends|");
            backendName =  StringUtils.left(metricPath,metricPath.indexOf('|'));
            if (sanitize)  {
                backendName = sanitize(backendName);
                application = sanitize(application);
            }
            tags.add("tier:"+ backendName);
            tags.add("backend:"+ backendName);
            tags.add("backendtype:tier");
        }


        AppDynamicsBackend backend = backends.get(backendName);
        if (backend != null) {
            tags.add("backendtype:" + backend.getExitPointType().toLowerCase());
            for (Object key : backend.getProperties().keySet()) {
                String keyStr = key.toString().toLowerCase();
                String valueStr = backend.getProperties().get(key).toString().toLowerCase();
                if (sanitize) {
                        keyStr = sanitize(keyStr);
                        valueStr = sanitize(valueStr);

                }
                tags.add(keyStr + ":" + valueStr);
            }
        }

        tags.add("application:" + application);

        return tags;
    }


    //Application Infrastructure Performance|WebTier|Advanced Network|Flows|Call from WebTier to InventoryTier_LB[VIP:54.149.131.192:80]
    private static JsonArray addTagsForFlows(JsonArray tags,AppDynamicsMetric metric, MetricUploadRequest request, boolean sanitize) {
        String metricPath = metric.getMetricPath();
        metricPath = StringUtils.remove(metricPath, "Application Infrastructure Performance|");
        String tierName = StringUtils.left(metricPath,metricPath.indexOf('|'));
        metricPath = StringUtils.remove(metricPath, tierName + "|Advanced Network|Flows|");
        String callPath = StringUtils.left(metricPath, metricPath.indexOf('|'));
        if (sanitize) {
            tierName = sanitize(tierName);
            callPath = sanitize(callPath);
        }
        tags.add("tier:" + tierName);
        tags.add("flow:" + callPath.toLowerCase().replace(' ','_'));
        return tags;
    }

    public static  String sanitize(String input) {
        input = StringUtils.replace(input, "Errors per Minute","EPM");
        input = StringUtils.replace(input, "Calls per Minute","CPM");
        input = StringUtils.replace(input, "Average Response Time","ART");
        input = StringUtils.replace(input," ","_");

        input = StringUtils.replace(input, "name", "_name");

        input = StringUtils.replace(input, "host", "_name");
        input = StringUtils.replace(input, "(ms)", ".ms");
        input = StringUtils.replace(input, "|", ".");
        input = StringUtils.replace(input, "/", "_");
        input = StringUtils.replace(input, "{", "");
        input = StringUtils.replace(input, "}", "");
        input = StringUtils.replace(input, "[", "_");
        input = StringUtils.replace(input, "]", "_");
        input = StringUtils.replace(input, "(", "_");
        input = StringUtils.replace(input, ")", "_");
        input = StringUtils.replace(input, "-", "_");
        input = StringUtils.replace(input, "&", "_");
        input = StringUtils.replace(input, "%", "_");
        input = StringUtils.replace(input, "\\t", "_");
        if (input.length() >= MAX_LENGTH) {
            logger.log(Level.FINER, "Truncating due to length value:" + input + " will be:" + input.substring(0,MAX_LENGTH-1));
            input = input.substring(0,MAX_LENGTH-1);
        }
        return input;
    }


    private static JsonArray addTagsForBT(JsonArray tags, AppDynamicsMetric metric, MetricUploadRequest request, boolean sanitize) {
        Map<String, AppDynamicsTier> tierMap = request.getTiersMap();
        Map<String, BusinessTransaction> btMap = request.getBusinessTransactionMap();
        String metricPath = StringUtils.remove(metric.getMetricPath(), "Business Transaction Performance|Business Transactions|");
        String tierName = StringUtils.left(metricPath, metricPath.indexOf('|'));
        metricPath = StringUtils.remove(metricPath,tierName);
        metricPath = StringUtils.substringAfter(metricPath,"|");
        String bt_name = StringUtils.left(metricPath, metricPath.indexOf('|'));
        String application = request.getApplication().getName();
        if (sanitize) {
            tierName = sanitize(tierName);
            bt_name = sanitize(bt_name);
            application = sanitize(application);
        }
        tags.add("tier:"+ tierName);
        tags.add("bt_name:"+ bt_name);
        tags.add("application:"+ application);

        AppDynamicsTier tier = tierMap.get(tierName);
        if (tier!= null) {
            for (AppDynamicsNode node:  tier.getNodes()) {
                tags.add("machinename:" + node.getMachineName());
                tags.add("agenttype:" + node.getAgentType().toLowerCase());
                tags.add("operatingsystem:"+node.getMachineOSType());
                tags.add("appagentversion:"+node.getAppAgentVersion());
            }
        }

        BusinessTransaction bt = btMap.get(bt_name + tierName);
        if (bt!= null) {
            tags.add("isbackgroundprocess:" + bt.getBackground());
            tags.add("entrypointtype:" + bt.getEntryPointTypeString());
            tags.add("internalbtname:" + bt.getInternalName());

        }

        return tags;
    }

    public static String getMetricNameForBackend(String appdPath) {
        StringBuffer buf = new StringBuffer("appdynamics.backends.");
            appdPath = appdPath.toLowerCase();
            appdPath = appdPath.replace(' ', '_');
            appdPath = appdPath.substring(appdPath.lastIndexOf('|')+1);
            buf.append(appdPath);
        return buf.toString();
    }

    public static String getMetricNameForBT(String appdPath) {
        StringBuffer buf = new StringBuffer("appdynamics.business_transaction.");
        appdPath = appdPath.toLowerCase();
        appdPath = appdPath.replace(' ', '_');
        appdPath = appdPath.substring(appdPath.lastIndexOf('|')+1);
        buf.append(appdPath);
        return buf.toString();
    }

    public static String getMetricNameForNetViz(String appdPath) {
        StringBuffer buf = new StringBuffer("appdynamics.network.");
            appdPath = appdPath.toLowerCase();

            appdPath = StringUtils.remove(appdPath, " - ");
            appdPath = appdPath.substring(appdPath.lastIndexOf('|') +1);
            appdPath = StringUtils.remove(appdPath,"# ");

            if ((appdPath.indexOf('(')>-1) && (appdPath.indexOf(')')>-1)) {
                    appdPath = appdPath.substring(0,appdPath.indexOf('(')).trim();
            }

            appdPath = appdPath.replace(' ', '.');
            buf.append(appdPath);

        return buf.toString();
    }

    public static String getMetricNameForCustom(String appdPath) {
        StringBuffer buf = new StringBuffer("appdynamics.custom_metric.");
        appdPath = StringUtils.remove(appdPath, "Application Infrastructure Performance|App Server|Custom Metrics|");
        appdPath = appdPath.toLowerCase();
        appdPath = StringUtils.remove(appdPath, " - ");
        appdPath = appdPath.replace(' ', '_');
        appdPath = appdPath.replace('|', '.');
        appdPath = appdPath.replace('/', '_');
        buf.append(appdPath);
        return buf.toString();
    }



    public static JsonObject toJsonObject(JsonArray array) {
        if (array != null) {
            JsonObject obj = new JsonObject();
            Iterator it = array.iterator();
            while (it.hasNext()) {
                JsonElement tag = (JsonElement) it.next();
                String jsonString = tag.getAsString();
                String[] parts = jsonString.split(":");
                obj.addProperty(parts[0], parts[1]);
            }
            return obj;
        }
        return null;
    }

    public static JsonElement createSeries(MetricUploadRequest request, boolean sanitize) {
        JsonObject series = new JsonObject();
        JsonArray metricArray = new JsonArray();


        for (AppDynamicsMetric metric: request.getMetrics()) {
            if (metric.getMetricValues().size() > 0) {
                String metricName = getMetricName(metric, sanitize);
                JsonObject metricObj = new JsonObject();
                metricObj.addProperty("metric", metricName);
                JsonArray points = new JsonArray();
                JsonArray point = new JsonArray();
                point.add(metric.getMetricValues().get(0).getStartTimeInMillis() / 1000);
                point.add(metric.getMetricValues().get(0).getValue());
                points.add(point);
                metricObj.add("points", points);
                JsonArray tags  = getTags(metric,request,sanitize);
                tags.add("source:appdynamics");
                metricObj.add("tags",tags);
                metricArray.add(metricObj);
            }
            series.add("series",metricArray);
        }

        return series;
    }

}
