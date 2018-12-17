package com.appdynamics.metricpump.appdynamics;

import com.appdynamics.metricpump.api.AbstractRESTEndpoint;

public class AppDynamicsEventServiceEndpoint extends AbstractRESTEndpoint {

    private String accountKey;
    private String apiKey;

    public AppDynamicsEventServiceEndpoint(String host, String accountKey, String apiKey) {
        super(host);
        this.accountKey = accountKey;
        this.apiKey = apiKey;
    }

    public String getAccountKey() {
        return accountKey;
    }

    public String getApiKey() {
        return apiKey;
    }
}
