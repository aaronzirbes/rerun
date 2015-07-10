package com.peoplenet.rerun.replay.domain

import com.peoplenet.rerun.replay.api.Source

class KafkaSource {
    String topic
    String partition
    Long startingOffset
    Long endingOffset

    Long offsetCount() {
        (endingOffset - startingOffset) + 1
    }

    Source toApi() {
       return new Source(
               topic: topic,
               partition: partition,
               startingOffset: startingOffset,
               endingOffset: endingOffset
       )
    }

    static KafkaSource fromApi(Source source) {
        return new KafkaSource(
                topic: source.topic,
                partition: source.partition,
                startingOffset: source.startingOffset,
                endingOffset: source.endingOffset
        )
    }
}
