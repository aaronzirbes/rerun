package com.peoplenet.rerun.replay.kafka

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * Consumes a range of offsets from a specific topic partition.
 */
@Slf4j
@CompileStatic
class RangeConsumer {
    private KafkaConnectionFactory connectionFactory

    RangeConsumer(KafkaConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory
    }

    /**
     *  Create a stream to consume the offset range from.
     *
     */
    RangedStream createStream(String topic, int partitionId, OffsetRange offsetRange)
            throws PartitionNotFoundException {

        PartitionInformation partitionInformation = connectionFactory.findLeader(topic, partitionId)

        return new RangedStream(connectionFactory, offsetRange, partitionInformation)
    }

}

