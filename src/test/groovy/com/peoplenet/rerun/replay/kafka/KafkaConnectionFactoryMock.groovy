package com.peoplenet.rerun.replay.kafka

import kafka.javaapi.consumer.SimpleConsumer

class KafkaConnectionFactoryMock extends KafkaConnectionFactory {
    SimpleConsumer simpleConsumer

    KafkaConnectionFactoryMock(SimpleConsumer mockConsumer, KafkaConfiguration config) {
        super(config)
        this.simpleConsumer = mockConsumer
    }

    @Override
    protected SimpleConsumer createSimpleConsumer(String host, int port, int soTimeout, int bufferSize, String clientId) {
        return simpleConsumer
    }
}
