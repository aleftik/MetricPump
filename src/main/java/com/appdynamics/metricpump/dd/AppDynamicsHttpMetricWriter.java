package com.appdynamics.metricpump.dd;

import com.appdynamics.metricpump.MetricUploadRequest;
import com.appdynamics.metricpump.MetricWriterUtilsV2;
import com.appdynamics.metricpump.api.AbstractHttpMerticWriter;

import com.google.gson.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import java.lang.reflect.Type;


public class AppDynamicsHttpMetricWriter extends AbstractHttpMerticWriter {


    public AppDynamicsHttpMetricWriter(String endPoint, String apiKey) {
       super(endPoint,apiKey);
    }

    protected Invocation.Builder getRequest() {
        Client client = ClientBuilder.newClient();
        return client.target(getEndpointURL()+"?api_key="+getApiKey()).request();
    }

    @Override
    public JsonElement serialize(MetricUploadRequest metricUploadRequest, Type typeOfSrc, JsonSerializationContext context) {
        return MetricWriterUtilsV2.createSeries(metricUploadRequest,false);
    }
}
