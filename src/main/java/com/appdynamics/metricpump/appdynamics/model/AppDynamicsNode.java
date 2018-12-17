package com.appdynamics.metricpump.appdynamics.model;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement (name="node")
@XmlAccessorType(XmlAccessType.FIELD)
public class AppDynamicsNode {

    @XmlElement
    private String machineName;
    @XmlElement
    private Long nodeId;
    @XmlElement
    private  String  machineOSType;
    @XmlElement
    private String machineAgentVersion;
    @XmlElement
    private boolean machineAgentPresent;
    @XmlElement
    private boolean appAgentPresent;
    @XmlElement
    private String appAgentVersion;
    @XmlElement
    private String agentType;
    @XmlElement(name="ipAddress")
    @XmlElementWrapper(name="ipAddresses")
    private List<String> ipAddresses;

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public String getMachineOSType() {
        return machineOSType;
    }

    public void setMachineOSType(String machineOSType) {
        this.machineOSType = machineOSType;
    }

    public String getMachineAgentVersion() {
        return machineAgentVersion;
    }

    public void setMachineAgentVersion(String machineAgentVersion) {
        this.machineAgentVersion = machineAgentVersion;
    }

    public boolean isMachineAgentPresent() {
        return machineAgentPresent;
    }

    public void setMachineAgentPresent(boolean machineAgentPresent) {
        this.machineAgentPresent = machineAgentPresent;
    }

    public boolean isAppAgentPresent() {
        return appAgentPresent;
    }

    public void setAppAgentPresent(boolean appAgentPresent) {
        this.appAgentPresent = appAgentPresent;
    }

    public String getAppAgentVersion() {
        return appAgentVersion;
    }

    public void setAppAgentVersion(String appAgentVersion) {
        this.appAgentVersion = appAgentVersion;
    }

    public String getAgentType() {
        return agentType;
    }

    public void setAgentType(String agentType) {
        this.agentType = agentType;
    }

    public List<String> getIpAddresses() {
        return ipAddresses;
    }

    public void setIpAddresses(List<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }
}
