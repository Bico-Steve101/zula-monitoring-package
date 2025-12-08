package com.zula.monitoring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "zula.monitoring")
public class MonitoringProperties {
    private boolean enabled = true;
    private String applicationName = "unknown-service";
    private boolean enablePrometheus = true;
    private boolean enableHealth = true;
    private boolean enableMetrics = true;
    private String serviceHost;
    private final RegistrationProperties registration = new RegistrationProperties();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public boolean isEnablePrometheus() {
        return enablePrometheus;
    }

    public void setEnablePrometheus(boolean enablePrometheus) {
        this.enablePrometheus = enablePrometheus;
    }

    public boolean isEnableHealth() {
        return enableHealth;
    }

    public void setEnableHealth(boolean enableHealth) {
        this.enableHealth = enableHealth;
    }

    public boolean isEnableMetrics() {
        return enableMetrics;
    }

    public void setEnableMetrics(boolean enableMetrics) {
        this.enableMetrics = enableMetrics;
    }

    public String getServiceHost() {
        return serviceHost;
    }

    public void setServiceHost(String serviceHost) {
        this.serviceHost = serviceHost;
    }

    public RegistrationProperties getRegistration() {
        return registration;
    }

    public static class RegistrationProperties {
        private final TargetProperties grafana = new TargetProperties();
        private final TargetProperties datahog = new TargetProperties();
        private final TargetProperties prometheus = new TargetProperties();
        private Duration timeout = Duration.ofSeconds(3);
        private String managementBasePath = "/actuator";
        private String metricsEndpoint = "/prometheus";
        private String healthEndpoint = "/health";

        public TargetProperties getGrafana() {
            return grafana;
        }

        public TargetProperties getDatahog() {
            return datahog;
        }

        public TargetProperties getPrometheus() {
            return prometheus;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }

        public String getManagementBasePath() {
            return managementBasePath;
        }

        public void setManagementBasePath(String managementBasePath) {
            this.managementBasePath = managementBasePath;
        }

        public String getMetricsEndpoint() {
            return metricsEndpoint;
        }

        public void setMetricsEndpoint(String metricsEndpoint) {
            this.metricsEndpoint = metricsEndpoint;
        }

        public String getHealthEndpoint() {
            return healthEndpoint;
        }

        public void setHealthEndpoint(String healthEndpoint) {
            this.healthEndpoint = healthEndpoint;
        }
    }

    public static class TargetProperties {
        private boolean enabled;
        private String url;
        private Map<String, String> metadata = new LinkedHashMap<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, String> metadata) {
            this.metadata = metadata == null ? new LinkedHashMap<>() : new LinkedHashMap<>(metadata);
        }
    }
}
