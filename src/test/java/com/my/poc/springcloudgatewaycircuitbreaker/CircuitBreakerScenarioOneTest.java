package com.my.poc.springcloudgatewaycircuitbreaker;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.my.poc.springcloudgatewaycircuitbreaker.config.CircuitBreakerState;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.FluxExchangeResult;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Slf4j
public class CircuitBreakerScenarioOneTest extends BaseGatewayTest {

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(7000);

    int i = 0;

    @BeforeClass
    public static void init() {
        System.setProperty("sliding.window.size", "6");
        System.setProperty("permitted.calls.half.open.state", "3");
        System.setProperty("failure.rate.threshold", "50");
        System.setProperty("wait.duration.open.state.milliseconds", "10");
        System.setProperty("timeout.duration.milliseconds", "300");
        System.setProperty("circuit.breaker.identifier.default", "default");
    }

    @Test
    @BenchmarkOptions(warmupRounds = 0, concurrency = 1, benchmarkRounds = 20)
    public void test() {
        stubFor(get(urlEqualTo("/api/whatever/1"))
                .willReturn(aResponse()
                        .withStatus(200)));

        stubFor(get(urlEqualTo("/api/whatever/2"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withFixedDelay(500)));

        int gen = 1 + (i++ % 2);

        circuitBreaker = circuitBreakerRegistry.circuitBreaker(System.getProperty("circuit.breaker.identifier.default"));

        FluxExchangeResult<String> stringFluxExchangeResult = testClient.get().uri("/api/whatever/" + gen)
                .exchange().returnResult(String.class);

        String circuitBreakerState = CircuitBreakerState.getDesc(circuitBreaker.getState().getOrder());
        log.info("Received: " + i + ", call: " + gen + ", status: " + stringFluxExchangeResult.getStatus().toString());
        log.info("Circuit breaker state: " + circuitBreakerState);

        switch(i) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
                assertThat(circuitBreakerState).isEqualTo("Closed");
                break;
            case 6:
            case 15:
            case 18:
                assertThat(circuitBreakerState).isEqualTo("Open");
                break;
            case 7:
            case 8:
            case 16:
            case 17:
            case 19:
            case 20:
                assertThat(circuitBreakerState).isEqualTo("Half Open");
                break;
            default:
                break;
        }
    }
}
