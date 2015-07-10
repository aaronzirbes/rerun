package com.peoplenet.rerun.replay.kafka

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode

@CompileStatic
@EqualsAndHashCode
class BrokerAddress {
    static final Integer DEFAULT_PORT = 9092
    String host
    Integer port

    BrokerAddress(String host, Integer port) {
        this.host = host
        this.port = port
    }

    BrokerAddress(String host) {
        this.host = host
        this.port = DEFAULT_PORT
    }

    static BrokerAddress from(String brokerAddress) {

        if(brokerAddress) {
            String[] parts = brokerAddress.split(':')

            if (parts.size() == 2) {
                return new BrokerAddress(parts[0], parts[1].toInteger())
            } else if (parts.size() == 1) {
                return new BrokerAddress(parts[0])
            }
        }

        throw new IllegalArgumentException('Invalid broker address format, expected <host[:port]>')
    }

    String toString() {
        "${host}:${port}"
    }
}
