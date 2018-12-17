package com.appdynamics.metricpump.appdynamics.model;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "name-value")
@XmlAccessorType(XmlAccessType.FIELD)
public class AppDynamicsBackendProperty {
    @XmlElement
    private String name;
    @XmlElement
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
