package com.my.poc.springcloudgatewaycircuitbreaker.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;

@Configuration
public class CustomCircuitBreakerConfig {

    @Value("${sliding.window.size}")
    private Integer slidingWindowSize;

    @Value("${permitted.calls.half.open.state}")
    private Integer permittedCallsHalfOpenState;

    @Value("${failure.rate.threshold}")
    private Integer failureRateThreshold;

    @Value("${wait.duration.open.state.milliseconds}")
    private Integer waitDurationOpenState;

    @Value("${timeout.duration.milliseconds}")
    private Integer timeoutDuration;

    @Value("${circuit.breaker.identifier.default}")
    private String circuitBreakerIdentifier;

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> customizer() {
        return factory -> reactiveResilience4JCircuitBreakerFactory();
    }

    @Bean
    public ReactiveResilience4JCircuitBreakerFactory reactiveResilience4JCircuitBreakerFactory() {
        ReactiveResilience4JCircuitBreakerFactory reactiveResilience4JCircuitBreakerFactory =
                new ReactiveResilience4JCircuitBreakerFactory();
        reactiveResilience4JCircuitBreakerFactory.configureCircuitBreakerRegistry(circuitBreakerRegistry());
        reactiveResilience4JCircuitBreakerFactory.configureDefault(id -> new Resilience4JConfigBuilder(circuitBreakerIdentifier)
                .circuitBreakerConfig(circuitBreakerConfig())
                .timeLimiterConfig(timeLimiterConfig())
                .build());
        return reactiveResilience4JCircuitBreakerFactory;
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        HashMap<String, CircuitBreakerConfig> circuitBreakerConfigHashMap = new HashMap<>();
        circuitBreakerConfigHashMap.put(circuitBreakerIdentifier, circuitBreakerConfig());
        return CircuitBreakerRegistry.of(circuitBreakerConfigHashMap);
    }

    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .slidingWindowSize(slidingWindowSize)
                .permittedNumberOfCallsInHalfOpenState(permittedCallsHalfOpenState)
                .failureRateThreshold(failureRateThreshold.floatValue())
                .waitDurationInOpenState(Duration.ofMillis(waitDurationOpenState))
                .build();
    }

    @Bean
    public TimeLimiterConfig timeLimiterConfig() {
        return TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(timeoutDuration)).build();
    }
}
