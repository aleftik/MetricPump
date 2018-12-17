package com.appdynamics.metricpump.dd;

import com.appdynamics.metricpump.EventUploadRequest;
import com.appdynamics.metricpump.api.AbstractHttpEventWriter;
import com.appdynamics.metricpump.appdynamics.model.AppDynamicsBrowserEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import java.lang.reflect.Type;

public class DataDogEventHttpWriter extends AbstractHttpEventWriter {

    public DataDogEventHttpWriter(String endPoint, String apiKey) {
        super(endPoint, apiKey);
    }

    @Override
    public JsonElement serialize(EventUploadRequest eventUploadRequest, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject series = new JsonObject();
        JsonArray metricArray = new JsonArray();
        series.add("series",metricArray);

        for (AppDynamicsBrowserEvent event : eventUploadRequest.getEvents()) {
            JsonArray tags = generateTags(event);

            if (event.getTotalEndUserTime() != null) {
                JsonObject endUserTotalTime = new JsonObject();
                metricArray.add(endUserTotalTime);
                endUserTotalTime.addProperty("metric", "end_user.browser.total_end_user_time");
                JsonArray endUserPoints = new JsonArray();
                endUserTotalTime.addProperty("type", "gauge");
                endUserTotalTime.add("points", endUserPoints);
                JsonArray endUserTotalTimeTags = new JsonArray();
                endUserTotalTime.add("tags", endUserTotalTimeTags);


                JsonArray endUserDataPoint = new JsonArray();
                endUserDataPoint.add(event.getEventTimestamp());
                endUserDataPoint.add(event.getTotalEndUserTime());
                endUserPoints.add(endUserDataPoint);
                endUserTotalTimeTags.addAll(tags);
            }

            if (event.getRequestsPerMinute()!= null) {
                JsonObject requestPerMinute = new JsonObject();
                metricArray.add(requestPerMinute);
                requestPerMinute.addProperty("metric", "end_user.browser.requests_per_minute");
                requestPerMinute.addProperty("type", "gauge");
                JsonArray rpmPoints = new JsonArray();
                requestPerMinute.add("points", rpmPoints);
                JsonArray rpmTags = new JsonArray();
                requestPerMinute.add("tags", rpmTags);

                JsonArray requestPerMinuteDataPoint = new JsonArray();
                requestPerMinuteDataPoint.add(event.getEventTimestamp());
                requestPerMinuteDataPoint.add(event.getRequestsPerMinute());
                rpmPoints.add(requestPerMinuteDataPoint);
                rpmTags.addAll(tags);

            }
    
            if(event.getResponseAvailbleTime() != null) {
                JsonObject responseAvailableTime = new JsonObject();
                metricArray.add(responseAvailableTime);
                responseAvailableTime.addProperty("metric", "end_user.browser.response_available_time");
                responseAvailableTime.addProperty("type", "gauge");
                JsonArray responseAvailablePoints = new JsonArray();
                responseAvailableTime.add("points", responseAvailablePoints);

                JsonArray responseAvailableTimeTags = new JsonArray();
                responseAvailableTime.add("tags", responseAvailableTimeTags);
                JsonArray responseAvailableDataPoint = new JsonArray();
                responseAvailableDataPoint.add(event.getEventTimestamp());
                responseAvailableDataPoint.add(event.getRequestsPerMinute());
                responseAvailablePoints.add(responseAvailableDataPoint);
                responseAvailableTimeTags.addAll(tags);
            }
        }
        return series;
    }

    private JsonArray generateTags(AppDynamicsBrowserEvent event) {
        JsonArray tags = new JsonArray();
        tags.add("pagename:"+ event.getPagename());
        tags.add("browser:"+ event.getBrowser());
        tags.add("browser_version:" + event.getBrowserversion());
        tags.add("page_type:" + event.getPagetype());
        tags.add("geocity:"+ event.getGeocity());
        tags.add("client_ip:"+ event.getIp());
        tags.add("page_url:"+ event.getPageurl());
        return tags;
    }
}