package com.zula.monitoring.config;

import com.zula.monitoring.core.MetricsService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@AutoConfiguration
@ConditionalOnClass(MeterRegistry.class)
@EnableConfigurationProperties(MonitoringProperties.class)
public class MonitoringAutoConfig {

    @Bean
    @ConditionalOnMissingBean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(MonitoringProperties properties) {
        return registry -> registry.config().commonTags("application", properties.getApplicationName());
    }

    @Bean
    @ConditionalOnMissingBean
    public MetricsService metricsService(MeterRegistry meterRegistry) {
        return new MetricsService(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "monitoringRestTemplate")
    public RestTemplate monitoringRestTemplate(MonitoringProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        Duration timeout = properties.getRegistration().getTimeout();
        if (timeout == null || timeout.isNegative() || timeout.isZero()) {
            timeout = Duration.ofSeconds(3);
        }
        int timeoutMs = (int) Math.min(timeout.toMillis(), Integer.MAX_VALUE);
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        return new RestTemplate(factory);
    }

    @Bean
    @ConditionalOnMissingBean
    public MonitoringRegistrar monitoringRegistrar(MonitoringProperties properties,
                                                   Environment environment,
                                                   @Qualifier("monitoringRestTemplate") RestTemplate restTemplate) {
        return new MonitoringRegistrar(properties, environment, restTemplate);
    }
}
