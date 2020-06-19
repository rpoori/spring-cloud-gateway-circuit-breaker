package com.my.poc.springcloudgatewaycircuitbreaker;

import com.my.poc.springcloudgatewaycircuitbreaker.config.CircuitBreakerState;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RequiredArgsConstructor
@RestController
@Slf4j
public class SpringCloudGatewayCircuitBreakerApplication {

	private final CircuitBreakerRegistry circuitBreakerRegistry;
	private final TimeLimiterConfig timeLimiterConfig;

	@Value("${circuit.breaker.identifier.default}")
	private String circuitBreakerIdentifier;

	public static void main(String[] args) {
		SpringApplication.run(SpringCloudGatewayCircuitBreakerApplication.class, args);
	}

	@GetMapping("/fallback")
	public ResponseEntity fallback() {
		CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(circuitBreakerIdentifier);
		String circuitBreakerState = CircuitBreakerState.getDesc(circuitBreaker.getState().getOrder());
		log.info("Executing fallback for circuit breaker: " + circuitBreakerIdentifier + ", circuit breaker state: " + circuitBreakerState);
		log.info("Circuit breaker sliding window size: " + circuitBreaker.getCircuitBreakerConfig().getSlidingWindowSize());
		log.info("Circuit breaker permitted calls half open state: " + circuitBreaker.getCircuitBreakerConfig().getPermittedNumberOfCallsInHalfOpenState());
		log.info("Circuit breaker failure rate threshold: " + circuitBreaker.getCircuitBreakerConfig().getFailureRateThreshold());
		log.info("Circuit breaker wait duration open state milliseconds: " + circuitBreaker.getCircuitBreakerConfig().getWaitDurationInOpenState());
		log.info("Circuit breaker timeout duration milliseconds: " + timeLimiterConfig.getTimeoutDuration());

		log.info("Circuit breaker metrics number of successful calls: " + circuitBreaker.getMetrics().getNumberOfSuccessfulCalls());
		log.info("Circuit breaker metrics failure rate: " + circuitBreaker.getMetrics().getFailureRate());
		log.info("Circuit breaker metrics number of buffered calls: " + circuitBreaker.getMetrics().getNumberOfBufferedCalls());
		log.info("Circuit breaker metrics number of failed calls: " + circuitBreaker.getMetrics().getNumberOfFailedCalls());
		log.info("Circuit breaker metrics number of not permitted calls: " + circuitBreaker.getMetrics().getNumberOfNotPermittedCalls());
		log.info("Circuit breaker metrics number of slow calls: " + circuitBreaker.getMetrics().getNumberOfSlowCalls());
		log.info("Circuit breaker metrics number of slow failed calls: " + circuitBreaker.getMetrics().getNumberOfSlowFailedCalls());
		log.info("Circuit breaker metrics number of slow successful calls: " + circuitBreaker.getMetrics().getNumberOfSlowSuccessfulCalls());
		log.info("Circuit breaker metrics slow call rate: " + circuitBreaker.getMetrics().getSlowCallRate());

		return new ResponseEntity(HttpStatus.FAILED_DEPENDENCY);
	}
}
