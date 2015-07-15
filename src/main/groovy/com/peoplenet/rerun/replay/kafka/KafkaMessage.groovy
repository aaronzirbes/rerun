package com.peoplenet.rerun.replay.kafka

import groovy.transform.CompileStatic
import groovy.transform.ToString


@CompileStatic
@ToString
class KafkaMessage {
    byte[] payload
    String topic
    Integer partitionId
    Long offset
}
