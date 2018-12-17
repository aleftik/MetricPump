package com.appdynamics.metricpump;

import com.appdynamics.metricpump.appdynamics.model.AppDynamicsBrowserEvent;

import java.util.ArrayList;
import java.util.List;

public class EventUploadRequest {
    private List<AppDynamicsBrowserEvent> events = new ArrayList<AppDynamicsBrowserEvent>();

    public EventUploadRequest(List<AppDynamicsBrowserEvent> events) {
        this.events = events;
    }

    public List<AppDynamicsBrowserEvent> getEvents() {
        return events;
    }
}
