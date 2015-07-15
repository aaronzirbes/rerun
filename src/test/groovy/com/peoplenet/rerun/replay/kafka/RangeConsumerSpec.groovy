package com.peoplenet.rerun.replay.kafka

import spock.lang.Specification

class RangeConsumerSpec extends Specification {

    void 'test create stream'() {
        given:
        KafkaConnectionFactory factory = Mock(KafkaConnectionFactory)
        RangeConsumer consumer = new RangeConsumer(factory)

        String topic = 'test'
        int partition = 0
        OffsetRange range = new OffsetRange(0l, 10l)


        when:
        RangedStream stream = consumer.createStream(topic, partition, range)

        then:
        1 * factory.findLeader(topic, partition) >> new PartitionInformation(
                partitionId: partition,
                leader: BrokerAddress.from('localhost:9092'),
                topic: topic)
        0 * _

        stream != null
    }
}
