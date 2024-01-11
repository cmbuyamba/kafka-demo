package com.ugovia.kafka.kafkademo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventLog {
    private String oid;
    private String variable;
    private String deviceIp;

}
