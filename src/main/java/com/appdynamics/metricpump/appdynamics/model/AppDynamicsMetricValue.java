package com.appdynamics.metricpump.appdynamics.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/*
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
 */

@XmlRootElement(name="metric-value")
@XmlAccessorType(XmlAccessType.FIELD)
public class AppDynamicsMetricValue {
    @XmlElement
    private Long startTimeInMillis;
    @XmlElement
    private Long value;
    @XmlElement
    private Long min;
    @XmlElement
    private Long max;
    @XmlElement
    private Long current;
    @XmlElement
    private Long sum;
    @XmlElement
    private Long count;
    @XmlElement
    private Double standardDeviation;
    @XmlElement
    private Long occurrences;
    @XmlElement
    private Boolean useRange;

    public Long getStartTimeInMillis() {
        return startTimeInMillis;
    }

    public void setStartTimeInMillis(Long startTimeInMillis) {
        this.startTimeInMillis = startTimeInMillis;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public Long getMin() {
        return min;
    }

    public void setMin(Long min) {
        this.min = min;
    }

    public Long getMax() {
        return max;
    }

    public void setMax(Long max) {
        this.max = max;
    }

    public Long getCurrent() {
        return current;
    }

    public void setCurrent(Long current) {
        this.current = current;
    }

    public Long getSum() {
        return sum;
    }

    public void setSum(Long sum) {
        this.sum = sum;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Double getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(Double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public Long getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(Long occurrences) {
        this.occurrences = occurrences;
    }

    public Boolean getUseRange() {
        return useRange;
    }

    public void setUseRange(Boolean useRange) {
        this.useRange = useRange;
    }
}
