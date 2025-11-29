package com.zula.monitoring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zula.monitoring")
public class MonitoringProperties {
    private boolean enabled = true;
    private String applicationName = "unknown-service";
    private boolean enablePrometheus = true;
    private boolean enableHealth = true;
    private boolean enableMetrics = true;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getApplicationName() { return applicationName; }
    public void setApplicationName(String applicationName) { this.applicationName = applicationName; }

    public boolean isEnablePrometheus() { return enablePrometheus; }
    public void setEnablePrometheus(boolean enablePrometheus) { this.enablePrometheus = enablePrometheus; }

    public boolean isEnableHealth() { return enableHealth; }
    public void setEnableHealth(boolean enableHealth) { this.enableHealth = enableHealth; }

    public boolean isEnableMetrics() { return enableMetrics; }
    public void setEnableMetrics(boolean enableMetrics) { this.enableMetrics = enableMetrics; }
}