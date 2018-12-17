package com.appdynamics.metricpump.api;

import com.appdynamics.metricpump.EventUploadRequest;
import com.google.gson.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractHttpEventWriter  implements JsonSerializer<EventUploadRequest>, EventWriter {
    private Logger logger = Logger.getLogger(AbstractHttpMerticWriter.class.getName());

    private String endpointURL = null;
    private String apiKey;


    public AbstractHttpEventWriter(String endPoint, String apiKey) {
        this.endpointURL = endPoint;
        this.apiKey = apiKey;
    }



    public void write(EventUploadRequest request) {
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(EventUploadRequest.class, this);
            Gson gson = gsonBuilder.create();
            String json = gson.toJson(request);
            postMetrics(json);

        } catch (Exception ie) {
            logger.log(Level.SEVERE,"Error Writing Http Metrics",ie);
        }

    }

    public void postMetrics(String json) {
        Client client = ClientBuilder.newClient();
        Response response = getRequest().post(Entity.json(json));
        logger.info("Response:" + response.getStatus());
    }

    protected Invocation.Builder getRequest() {
        Client client = ClientBuilder.newClient();

        return client.target(this.endpointURL + "?api_key="+ getApiKey()).request();
    }

    public String getEndpointURL() {
        return endpointURL;
    }

    public String getApiKey() {
        return apiKey;
    }

    public abstract JsonElement serialize(EventUploadRequest metricUploadRequest, Type typeOfSrc, JsonSerializationContext context);
}
