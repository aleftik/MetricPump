package com.appdynamics.metricpump.appdynamics.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;




/*

   <business-transaction>
     <id>92</id>
     <name>/user/.POST</name>
     <entryPointType>WEB_SERVICE</entryPointType>
     <internalName>/user/.POST</internalName>
     <tierId>9</tierId>
     <tierName>ECommerce-Services</tierName>
     <background>false</background>
   </business-transaction>
 */
@XmlRootElement(name = "business-transaction")
@XmlAccessorType(XmlAccessType.FIELD)
public class BusinessTransaction {
    @XmlElement
    private Long id;
    @XmlElement
    private String name;
    @XmlElement(name = "entryPointType")
    private String entryPointType;
    @XmlElement(name="entryPointTypeString")
    private String entryPointTypeString;
    @XmlElement
    private String internalName;
    @XmlElement
    private Long tierId;
    @XmlElement
    private String tierName;
    @XmlElement
    private Boolean isBackground;
    private Long appId;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEntryPointType() {
        return entryPointType;
    }

    public void setEntryPointType(String entryPointType) {
        this.entryPointType = entryPointType;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public Long getTierId() {
        return tierId;
    }

    public void setTierId(Long tierId) {
        this.tierId = tierId;
    }

    public String getTierName() {
        return tierName;
    }

    public void setTierName(String tierName) {
        this.tierName = tierName;
    }

    public String getEntryPointTypeString() {
        return entryPointTypeString;
    }

    public void setEntryPointTypeString(String entryPointTypeString) {
        this.entryPointTypeString = entryPointTypeString;
    }

    public Boolean getBackground() {
        return isBackground;
    }

    public void setBackground(Boolean background) {
        isBackground = background;
    }


    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getKey() {
        return appId + "-" + getName() + getTierName();
    }
}
