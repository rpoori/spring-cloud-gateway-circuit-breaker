package com.my.poc.springcloudgatewaycircuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Slf4j
public class BaseGatewayTest {

    @LocalServerPort
    protected int port = 0;

    protected String baseUri;
    protected WebClient webClient;
    protected WebTestClient testClient;

    @Autowired
    protected CircuitBreakerRegistry circuitBreakerRegistry;

    protected CircuitBreaker circuitBreaker;

    @Before
    public void setup() {
        ClientHttpConnector httpConnector = new ReactorClientHttpConnector();

        baseUri = "http://localhost:" + port;

        this.webClient = WebClient.builder()
                .clientConnector(httpConnector)
                .baseUrl(baseUri)
                .build();

        this.testClient = WebTestClient.bindToServer()
                .baseUrl(baseUri)
                .responseTimeout(Duration.ofSeconds(30L))
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(500000))
                        .build())
                .build();
    }

}
