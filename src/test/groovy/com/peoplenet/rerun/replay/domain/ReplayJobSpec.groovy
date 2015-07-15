package com.peoplenet.rerun.replay.domain

import spock.lang.Specification
import spock.lang.Unroll

class ReplayJobSpec extends Specification {

    @Unroll
    void 'Checkpoint calculates percentage correctly'() {
        given: 'A replay good replay job'
        ReplayJob replayJob = new ReplayJob('123',
                new ReplayJobRequest(source: new KafkaSource(
                        startingOffset: start,
                        endingOffset: end
                )))

        when:
        replayJob.checkpoint(current)

        then:
        replayJob.percentComplete == expected

        where:
        start   | end    | current | expected
        0       | 0      | 0       | 100
        1       | 1      | 1       | 100
        0       | 1      | 0       | 50
        0       | 1      | 1       | 100
        0       | 9      | 7       | 80
        0       | 2      | 1       | 66
        0       | 2      | 0       | 33
    }
}
