package com.my.poc.springcloudgatewaycircuitbreaker.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum CircuitBreakerState {
    CLOSED(0, "Closed"),
    OPEN(1, "Open"),
    HALF_OPEN(2, "Half Open"),
    DISABLED(3, "Disabled"),
    FORCED_OPEN(4, "Forced Open");

    @Getter
    private Integer code;

    @Getter
    private String description;

    public static String getDesc(Integer code){
        for(CircuitBreakerState circuitBreakerState: CircuitBreakerState.values()){
            if(circuitBreakerState.getCode().equals(code)){
                return circuitBreakerState.getDescription();
            }
        }
        return null;
    }
}
