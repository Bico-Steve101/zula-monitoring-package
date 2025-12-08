package com.zula.monitoring.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MonitoringRegistrar {
    private static final Logger log = LoggerFactory.getLogger(MonitoringRegistrar.class);

    private final MonitoringProperties properties;
    private final Environment environment;
    private final RestTemplate restTemplate;
    private final AtomicBoolean registered = new AtomicBoolean();

    public MonitoringRegistrar(MonitoringProperties properties, Environment environment, RestTemplate restTemplate) {
        this.properties = properties;
        this.environment = environment;
        this.restTemplate = restTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registerOnStartup() {
        if (!properties.isEnabled()) {
            log.debug("Monitoring registration disabled via configuration");
            return;
        }

        if (!registered.compareAndSet(false, true)) {
            return;
        }

        MonitoringProperties.RegistrationProperties registration = properties.getRegistration();
        String scheme = determineScheme();
        String host = resolveHost();
        int servicePort = determineServicePort();
        int managementPort = determineManagementPort(servicePort);
        String serviceUrl = buildBaseUrl(scheme, host, servicePort);
        String managementUrl = buildBaseUrl(scheme, host, managementPort);
        String environmentName = determineEnvironment();

        registerTarget("grafana", registration.getGrafana(), registration, serviceUrl, managementUrl, environmentName);
        registerTarget("datahog", registration.getDatahog(), registration, serviceUrl, managementUrl, environmentName);
        registerTarget("prometheus", registration.getPrometheus(), registration, serviceUrl, managementUrl, environmentName);
    }

    private void registerTarget(String targetName,
                                MonitoringProperties.TargetProperties target,
                                MonitoringProperties.RegistrationProperties registration,
                                String serviceUrl,
                                String managementUrl,
                                String environmentName) {
        if (target == null || !target.isEnabled() || !StringUtils.hasText(target.getUrl())) {
            return;
        }

        Map<String, Object> payload = buildPayload(targetName, target, registration, serviceUrl, managementUrl, environmentName);

        try {
            restTemplate.postForEntity(target.getUrl(), payload, Void.class);
            log.info("Registered service [{}] with {} via {}", properties.getApplicationName(), targetName, target.getUrl());
        } catch (RestClientException ex) {
            log.warn("Failed to register service [{}] with {} at {}: {}", properties.getApplicationName(), targetName, target.getUrl(), ex.getMessage());
        }
    }

    private Map<String, Object> buildPayload(String targetName,
                                             MonitoringProperties.TargetProperties target,
                                             MonitoringProperties.RegistrationProperties registration,
                                             String serviceUrl,
                                             String managementUrl,
                                             String environmentName) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("target", targetName);
        payload.put("serviceName", properties.getApplicationName());
        payload.put("applicationName", properties.getApplicationName());
        payload.put("environment", environmentName);
        payload.put("serviceUrl", serviceUrl);
        payload.put("managementUrl", managementUrl);
        payload.put("healthUrl", buildManagementEndpoint(managementUrl, registration, registration.getHealthEndpoint()));
        payload.put("metricsUrl", buildManagementEndpoint(managementUrl, registration, registration.getMetricsEndpoint()));
        payload.put("metadata", new LinkedHashMap<>(target.getMetadata()));
        payload.put("timestamp", Instant.now().toString());
        return payload;
    }

    private String buildManagementEndpoint(String managementUrl,
                                           MonitoringProperties.RegistrationProperties registration,
                                           String endpoint) {
        String basePath = registration.getManagementBasePath();
        if (!StringUtils.hasText(basePath)) {
            basePath = "/actuator";
        }

        String normalizedBase = normalizePath(basePath);
        String normalizedEndpoint = ensureLeadingSlash(endpoint);

        return managementUrl + normalizedBase + normalizedEndpoint;
    }

    private String buildBaseUrl(String scheme, String host, int port) {
        StringBuilder builder = new StringBuilder(scheme);
        builder.append("://").append(host);
        if (port > 0) {
            builder.append(":").append(port);
        }
        return builder.toString();
    }

    private int determineServicePort() {
        String portValue = environment.getProperty("local.server.port");
        if (!StringUtils.hasText(portValue)) {
            portValue = environment.getProperty("server.port");
        }
        return parsePort(portValue, 8080);
    }

    private int determineManagementPort(int servicePort) {
        String portValue = environment.getProperty("local.management.port");
        if (!StringUtils.hasText(portValue)) {
            portValue = environment.getProperty("management.server.port");
        }
        return parsePort(portValue, servicePort);
    }

    private int parsePort(String portValue, int fallback) {
        if (!StringUtils.hasText(portValue)) {
            return fallback;
        }
        try {
            return Integer.parseInt(portValue);
        } catch (NumberFormatException ex) {
            log.debug("Unable to parse port [{}], falling back to {}", portValue, fallback);
            return fallback;
        }
    }

    private String determineScheme() {
        return Boolean.parseBoolean(environment.getProperty("server.ssl.enabled", "false")) ? "https" : "http";
    }

    private String resolveHost() {
        String configured = properties.getServiceHost();
        if (StringUtils.hasText(configured)) {
            return configured;
        }

        String hostname = environment.getProperty("HOSTNAME");
        if (StringUtils.hasText(hostname)) {
            return hostname;
        }

        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            log.debug("Unable to resolve local host address, defaulting to localhost", ex);
            return "localhost";
        }
    }

    private String determineEnvironment() {
        String[] profiles = environment.getActiveProfiles();
        if (profiles.length == 0) {
            return "default";
        }
        return String.join(",", profiles);
    }

    private String normalizePath(String path) {
        if (!StringUtils.hasText(path)) {
            return "";
        }

        String normalized = path.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String ensureLeadingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = value.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized;
    }
}
