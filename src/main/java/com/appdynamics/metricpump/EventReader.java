package com.appdynamics.metricpump;

import com.appdynamics.metricpump.api.Reader;
import com.appdynamics.metricpump.appdynamics.AppDynamicsEventServiceEndpoint;
import com.appdynamics.metricpump.appdynamics.model.AppDynamicsBrowserEvent;
import com.google.gson.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class EventReader implements Reader,JsonDeserializer<EventUploadRequest> {
    private BlockingQueue<EventUploadRequest> queue;
    private AppDynamicsEventServiceEndpoint endpoint;
    private Gson gson;


    public EventReader(AppDynamicsEventServiceEndpoint endpoint ,BlockingQueue<EventUploadRequest> queue) {
        this.endpoint = endpoint;
        this.queue = queue;
        GsonBuilder build = new GsonBuilder();
        build.registerTypeAdapter(EventUploadRequest.class,this);
        gson = build.create();
    }


    public void read() {
        Client client = RESTClientFactory.create(endpoint);
        String query = "SELECT eventTimestamp,pagename, ip, pageurl, geocity, browser, pagetype, browserversion, metrics.`Requests per Minute`, metrics.`End User Response Time (ms)`, metrics.`Response Available Time (ms)` FROM browser_records";
        long startTime = System.currentTimeMillis()-(60*1000);
        Response response = client.target(endpoint.getHost() + "/events/query?start=" + startTime + "&end=" + System.currentTimeMillis() + "&label=pagedata")
                .request()
                .post(Entity.entity(query,"application/vnd.appd.events+json;v=2"));
        if (response.getStatus() == 200) {
            String resp = response.readEntity(String.class);

            EventUploadRequest events = gson.fromJson(resp, EventUploadRequest.class);

            try {
                queue.put(events);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public EventUploadRequest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<AppDynamicsBrowserEvent> events = new ArrayList<AppDynamicsBrowserEvent>();
        if (json instanceof JsonArray) {
            JsonArray response = (JsonArray) json;
            JsonObject returnValue = (JsonObject) response.get(0);
            JsonArray results = (JsonArray) returnValue.get("results");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

            for (int i = 0; i < results.size(); i++) {
                JsonArray record = (JsonArray)results.get(i);
                Date date = null;
                try {
                    date = sdf.parse(record.get(0).getAsString());
                } catch (ParseException pe) {
                    pe.printStackTrace();
                }

               AppDynamicsBrowserEvent event = new AppDynamicsBrowserEvent(
                       date.getTime()/1000,
                       record.get(1).getAsString(),
                       record.get(2).getAsString(),
                       record.get(3).getAsString(),
                       record.get(4).getAsString(),
                       record.get(5).getAsString(),
                       record.get(6).getAsString(),
                       record.get(7).getAsString(),
                       parseLong(record.get(8)),
                       parseLong(record.get(9)),
                       parseLong(record.get(10)));
               events.add(event);

            }
        }

        return new EventUploadRequest(events);
    }

    private Long parseLong(JsonElement jsonElement) {
        String value = jsonElement.toString();
        if ((value == null) || ("null").equals(value)) {
            return null;
        }

        return Long.parseLong(value);
    }
}
