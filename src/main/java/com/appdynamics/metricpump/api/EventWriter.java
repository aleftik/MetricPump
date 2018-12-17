package com.appdynamics.metricpump.api;

import com.appdynamics.metricpump.EventUploadRequest;

public interface EventWriter {
    void write(EventUploadRequest request);
}
