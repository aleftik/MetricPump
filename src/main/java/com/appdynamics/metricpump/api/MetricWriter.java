package com.appdynamics.metricpump.api;

import com.appdynamics.metricpump.MetricUploadRequest;

public interface MetricWriter {

    void write(MetricUploadRequest request);
}
