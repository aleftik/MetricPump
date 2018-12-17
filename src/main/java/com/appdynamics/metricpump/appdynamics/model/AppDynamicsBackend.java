package com.appdynamics.metricpump.appdynamics.model;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name="backend")
public class AppDynamicsBackend {
    @XmlElement
    private Long id;
    @XmlElement
    private String name;
    @XmlElement
    private String exitPointType;
    @XmlElement
    private String applicationComponentNodeId;
    @XmlElement
    private Long tierId;
    @XmlElementWrapper(name="properties")
    @XmlElement(name="name-value")
    private List<AppDynamicsBackendProperty> props;

    private Long appId;

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExitPointType() {
        return exitPointType;
    }

    public void setExitPointType(String exitPointType) {
        this.exitPointType = exitPointType;
    }

    public String getApplicationComponentNodeId() {
        return applicationComponentNodeId;
    }

    public void setApplicationComponentNodeId(String applicationComponentNodeId) {
        this.applicationComponentNodeId = applicationComponentNodeId;
    }

    public Long getTierId() {
        return tierId;
    }

    public void setTierId(Long tierId) {
        this.tierId = tierId;
    }

    public List<AppDynamicsBackendProperty> getProps() {
        return props;
    }

    public void setProps(List<AppDynamicsBackendProperty> props) {
        this.props = props;
    }

    public Map getProperties() {
        Map props = new HashMap<String, String>();
        for (AppDynamicsBackendProperty property:getProps()) {
            props.put(property.getName(),property.getValue());
        }
        return props;
    }

    public String getKey() {
        return appId + "-" + id;

    }

    @Override
    public String toString() {
        return getKey();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
