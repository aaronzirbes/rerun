package com.peoplenet.rerun.replay.kafka

import spock.lang.Specification
import spock.lang.Unroll

class BrokerAddressSpec extends Specification {

    void 'Test to string'() {
        expect:
        new BrokerAddress('host', 9999).toString() == 'host:9999'
        new BrokerAddress('host').toString() == 'host:9092'
    }

    @Unroll
    void 'Broker address is parsed correctly'() {
        when:
        BrokerAddress address = BrokerAddress.from(brokerStr)

        then:
        address.host == expectedHost
        address.port == expectedPort

        where:
        brokerStr           | expectedHost | expectedPort
        'host.host:9992'    | 'host.host'  | 9992
        'host.host'         | 'host.host'  | 9092
    }

    @Unroll
    void 'Broker address throws exception on bad address'() {
        when:
        BrokerAddress.from(badValue)

        then:
        thrown IllegalArgumentException

        where:
        badValue << ['', null, 'host:port:port']
    }
}
