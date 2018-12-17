package com.appdynamics.metricpump.api;

public abstract class AbstractRESTEndpoint {
    private String host = null;
    public AbstractRESTEndpoint(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }
}
