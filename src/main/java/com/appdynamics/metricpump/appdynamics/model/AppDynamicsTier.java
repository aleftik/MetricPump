package com.appdynamics.metricpump.appdynamics.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;

@XmlRootElement(name = "nodes")
public class AppDynamicsTier {
    @XmlTransient
    private String tierName;
    @XmlTransient
    private Long tierId;
    @XmlElement(name="node")
    private List<AppDynamicsNode> nodes;
    private Long appId;


    public String getTierName() {
        return tierName;
    }

    public void setTierName(String tierName) {
        this.tierName = tierName;
    }

    public Long getTierId() {
        return tierId;
    }

    public void setTierId(Long tierId) {
        this.tierId = tierId;
    }

    public List<AppDynamicsNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<AppDynamicsNode> nodes) {
        this.nodes = nodes;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getKey() {
        return appId + "-" + getTierName();
    }
}
