package com.appdynamics.metricpump.outlyer;

import com.appdynamics.metricpump.MetricUploadRequest;
import com.appdynamics.metricpump.MetricWriterUtilsV2;
import com.appdynamics.metricpump.api.AbstractHttpMerticWriter;

import com.appdynamics.metricpump.appdynamics.model.AppDynamicsMetric;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.Type;

import java.net.URI;
import java.net.URISyntaxException;


import java.util.logging.Level;
import java.util.logging.Logger;

public class OutlyerMerticWriter extends AbstractHttpMerticWriter {
    private static final Logger logger = Logger.getLogger(OutlyerMerticWriter.class.getName());
    private URI controller;


    public OutlyerMerticWriter(String endpoint, String apiKey, String controllerEndpoint) {
        super(endpoint,apiKey);
        try {
            this.controller = new URI(controllerEndpoint);
        } catch (URISyntaxException e) {
            logger.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    @Override
    protected Invocation.Builder getRequest() {
        return super.getRequest().header("Authorization", "Bearer " + getApiKey()).header("Content-Type", MediaType.APPLICATION_JSON_TYPE);

    }

    @Override
    public void postMetrics(String json) {
//        logger.severe(json);
        super.postMetrics(json);
    }

    @Override
    public JsonElement serialize(MetricUploadRequest metricUploadRequest, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject series = new JsonObject();
        JsonArray seriesArray = new JsonArray();
        for (AppDynamicsMetric metric:metricUploadRequest.getMetrics()) {
            if (metric.getMetricValues().size() > 0) {
                JsonObject dataPoint = new JsonObject();
                String metricPath = metric.getMetricPath();
                dataPoint.addProperty("host", this.controller.getHost());
                dataPoint.addProperty("name", MetricWriterUtilsV2.getMetricName(metric,true));
                dataPoint.addProperty("timestamp",metric.getMetricValues().get(0).getStartTimeInMillis());
                JsonObject labels = MetricWriterUtilsV2.toJsonObject(MetricWriterUtilsV2.getTags(metric,metricUploadRequest,true));
                dataPoint.add("labels",labels);

                if (metric.isCounter()) {
                    dataPoint.addProperty("type","counter");
                } else {
                    dataPoint.addProperty("type", "gauge");
                 }
                dataPoint.addProperty("value", metric.getMetricValues().get(0).getValue());
                seriesArray.add(dataPoint);
            }

        }
        series.add("samples",seriesArray);
        return series;
    }


}
