package com.peoplenet.rerun.replay.domain

import spock.lang.Specification
import spock.lang.Unroll

class KafkaSourceSpec extends Specification {

    @Unroll
    void 'Offset  count is correctly calculated'() {
        given: 'A good kafka source'
        KafkaSource source = new KafkaSource(
                startingOffset: start,
                endingOffset: end
        )

        when:
        Long count = source.offsetCount()

        then:
        count == expected

        where:
        start  | end  | expected
        0      | 0    | 1
        0      | 1    | 2
        0      | 5    | 6
    }
}
