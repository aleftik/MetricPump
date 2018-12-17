package com.appdynamics.metricpump.appdynamics;

import com.appdynamics.metricpump.api.AbstractRESTEndpoint;

public class AppDynamicsControllerEndpoint extends AbstractRESTEndpoint {
    private String username;
    private String password;

    public AppDynamicsControllerEndpoint(String host, String username, String password) {
        super(host);
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }


}
