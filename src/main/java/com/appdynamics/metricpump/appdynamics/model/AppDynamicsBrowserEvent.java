package com.appdynamics.metricpump.appdynamics.model;

public class AppDynamicsBrowserEvent {
    private long eventTimestamp;
    private String pagename;
    private String ip;
    private String pageurl;
    private String geocity;
    private String browser;
    private String pagetype;
    private String browserversion;
    private Long totalEndUserTime = null;
    private Long responseAvailbleTime = null;
    private Long requestsPerMinute = null;

    public AppDynamicsBrowserEvent(long eventTs,
                                   String pagename,
                                   String ip,
                                   String pageurl,
                                   String geocity,
                                   String browser,
                                   String pagetype,
                                   String browserversion,
                                   Long requestsPerMinute,
                                   Long totalEndUserTime,
                                   Long responseAvailbleTime) {

        this.eventTimestamp = eventTs;
        this.pagename = pagename;
        this.ip = ip;
        this.pageurl = pageurl;
        this.geocity = geocity;
        this.browser = browser;
        this.pagetype = pagetype;
        this.browserversion = browserversion;
        this.totalEndUserTime = totalEndUserTime;
        this.requestsPerMinute = requestsPerMinute;
        this.responseAvailbleTime =  responseAvailbleTime;
    }

    public String getPagename() {
        return pagename;
    }

    public String getIp() {
        return ip;
    }

    public String getPageurl() {
        return pageurl;
    }

    public String getGeocity() {
        return geocity;
    }

    public String getBrowser() {
        return browser;
    }

    public String getPagetype() {
        return pagetype;
    }

    public String getBrowserversion() {
        return browserversion;
    }

    public Long getTotalEndUserTime() {
        return totalEndUserTime;
    }

    public Long getResponseAvailbleTime() {
        return responseAvailbleTime;
    }

    public Long getRequestsPerMinute() {
        return requestsPerMinute;
    }

    public long getEventTimestamp() {
        return eventTimestamp;
    }
}
