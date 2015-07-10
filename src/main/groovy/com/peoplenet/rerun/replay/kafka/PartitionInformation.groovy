package com.peoplenet.rerun.replay.kafka

class PartitionInformation {
    BrokerAddress leader
    List<BrokerAddress> replicas
    Integer partitionId
    String topic
}
