package com.zula.monitoring.config;

import com.zula.monitoring.core.MetricsService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
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
}