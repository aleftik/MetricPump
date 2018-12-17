package com.appdynamics.metricpump.appdynamics.model;

import javax.xml.bind.annotation.*;
import java.util.List;

/*
  <metric-data>
  <metricId>2339</metricId>
  <metricPath>Overall Application Performance|Average Response Time (ms)</metricPath>
  <metricName>BTM|Application Summary|Average Response Time (ms)</metricName>
  <frequency>ONE_MIN</frequency>
  <metricValues>
    <metric-value>
      <startTimeInMillis>1450562160000</startTimeInMillis>
      <value>302</value>
      <min>0</min>
      <max>15212</max>
      <current>15212</current>
      <sum>97800</sum>
      <count>324</count>
      <standardDeviation>0.0</standardDeviation>
      <occurrences>0</occurrences>
      <useRange>true</useRange>
    </metric-value>
  </metricValues>
</metric-data>
 */
@XmlRootElement(name = "metric-data")
@XmlAccessorType(XmlAccessType.FIELD)
public class AppDynamicsMetric {
    @XmlElement
    private Long metricId;
    @XmlElement
    private String metricPath;
    @XmlElement
    private String metricName;
    @XmlElement
    private String frequency;
    @XmlElementWrapper(name="metricValues")
    @XmlElement(name="metric-value")
    List<AppDynamicsMetricValue> metricValues;
    @XmlTransient
    private String applicationName;

    public Long getMetricId() {
        return metricId;
    }

    public void setMetricId(Long metricId) {
        this.metricId = metricId;
    }

    public String getMetricPath() {
        return metricPath;
    }

    public void setMetricPath(String metricPath) {
        this.metricPath = metricPath;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public List<AppDynamicsMetricValue> getMetricValues() {
        return metricValues;
    }

    public void setMetricValues(List<AppDynamicsMetricValue> metricValues) {
        this.metricValues = metricValues;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public boolean isBackendMetric() {
        if (getMetricPath()== null) return false;
        if (getMetricPath().startsWith("Backend"))return true;
        return false;
    }

    public boolean isBusinessTransactionMetric() {
        if (getMetricPath()== null) return false;
        if (getMetricPath().startsWith("Business Transaction Performance"))return true;
        return false;
    }

    public boolean isNetworkVizMetric() {
        if (getMetricPath()== null) return false;
        if (getMetricPath().contains("Advanced Network"))return true;
        return false;
    }

    public boolean isCustomMetric() {
        if (getMetricPath()== null) return false;
        if (getMetricPath().contains("Custom Metrics"))return true;
        return false;
    }

    public boolean isCounter() {
        if (getMetricPath()== null) return false;
        if (getMetricPath().toLowerCase().contains("per minute") || getMetricPath().toLowerCase().contains("Stall Count")) return true;
        return false;
    }
}
