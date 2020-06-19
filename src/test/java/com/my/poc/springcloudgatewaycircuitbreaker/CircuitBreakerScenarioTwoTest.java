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
public class CircuitBreakerScenarioTwoTest extends BaseGatewayTest {

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(7000);

    int j = 0;

    @BeforeClass
    public static void init() {
        System.setProperty("sliding.window.size", "10");
        System.setProperty("permitted.calls.half.open.state", "5");
        System.setProperty("failure.rate.threshold", "50");
        System.setProperty("wait.duration.open.state.milliseconds", "10");
        System.setProperty("timeout.duration.milliseconds", "300");
        System.setProperty("circuit.breaker.identifier.default", "default");
    }

    @Test
    @BenchmarkOptions(warmupRounds = 0, concurrency = 1, benchmarkRounds = 30)
    public void test() {
        stubFor(get(urlEqualTo("/api/whatever/1"))
                .willReturn(aResponse()
                        .withStatus(200)));

        stubFor(get(urlEqualTo("/api/whatever/2"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withFixedDelay(500)));

        int gen = 1 + (j++ % 2);

        circuitBreaker = circuitBreakerRegistry.circuitBreaker(System.getProperty("circuit.breaker.identifier.default"));

        FluxExchangeResult<String> stringFluxExchangeResult = testClient.get().uri("/api/whatever/" + gen)
                .exchange().returnResult(String.class);

        String circuitBreakerState = CircuitBreakerState.getDesc(circuitBreaker.getState().getOrder());
        log.info("Received: " + j + ", call: " + gen + ", status: " + stringFluxExchangeResult.getStatus().toString());
        log.info("Circuit breaker state: " + circuitBreakerState);

        switch(j) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
                assertThat(circuitBreakerState).isEqualTo("Closed");
                break;
            case 10:
            case 25:
            case 30:
                assertThat(circuitBreakerState).isEqualTo("Open");
                break;
            case 11:
            case 12:
            case 13:
            case 14:
            case 26:
            case 27:
            case 28:
            case 29:
                assertThat(circuitBreakerState).isEqualTo("Half Open");
                break;
            default:
                break;
        }
    }
}
