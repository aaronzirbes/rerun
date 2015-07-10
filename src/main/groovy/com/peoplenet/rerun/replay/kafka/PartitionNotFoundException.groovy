package com.peoplenet.rerun.replay.kafka

class PartitionNotFoundException extends RuntimeException {
    String topic
    int partition

    PartitionNotFoundException(String topic, int partition) {
        super("Partition '${partition}' not found for topic: '${topic}'")
        this.topic = topic
        this.partition = partition
    }
}
