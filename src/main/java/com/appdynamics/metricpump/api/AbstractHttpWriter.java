package com.appdynamics.metricpump.api;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractHttpWriter {
    private Logger logger = Logger.getLogger(AbstractHttpWriter.class.getName());
    private String endpointURL = null;
    private String apiKey = null;
    private static ClientConfig clientConfig = null;

    static {
        clientConfig = new ClientConfig();
        clientConfig.property(ClientProperties.READ_TIMEOUT, TimeConstants.TWO_MINUTES);
        clientConfig.property(ClientProperties.CONNECT_TIMEOUT, TimeConstants.TWENTY_SECONDS);
        PoolingHttpClientConnectionManager connectionManager =
                new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(5000);
        connectionManager.setDefaultMaxPerRoute(5000);
        clientConfig.property(ApacheClientProperties.CONNECTION_MANAGER, connectionManager);
        clientConfig.property(ApacheClientProperties.CONNECTION_MANAGER_SHARED, true);
        clientConfig.connectorProvider(new ApacheConnectorProvider());
    }

    public AbstractHttpWriter (String endPoint, String apiKey) {
        this.endpointURL = endPoint;
        this.apiKey = apiKey;
    }

    public void postMetrics(String json) {
        Client client = ClientBuilder.newClient(AbstractHttpWriter.clientConfig);
        Response response = getRequest().post(Entity.json(json));
        logger.log(Level.FINEST, json);
        logger.log(Level.FINE,"Response Code:" + response.getStatus());
        String resp = response.readEntity(String.class);

        if ((response.getStatus() == 200) || (response.getStatus() == 202)) {
            logger.log(Level.FINEST,"Response:" + resp);
        } else {
            logger.log(Level.SEVERE,"Error Response:" + response.getStatus());
            logger.severe(resp);
        }

        close(response);
    }

    protected Invocation.Builder getRequest() {
        Client client = ClientBuilder.newClient();
        return client.target(this.endpointURL).request();
    }

    public String getEndpointURL() {
        return endpointURL;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void close(Response response) {
        if (response != null) {
            try {
                response.close();
            } catch (Exception ex) {
                logger.severe(ex.toString());
            }
        }
    }
}
