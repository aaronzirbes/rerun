package com.peoplenet.rerun.replay.service

import com.peoplenet.rerun.replay.domain.*
import com.peoplenet.rerun.replay.forward.ForwarderException
import com.peoplenet.rerun.replay.forward.HttpForwarder
import com.peoplenet.rerun.replay.forward.HttpForwarderConfig
import com.peoplenet.rerun.replay.job.ReplayJobRepo
import com.peoplenet.rerun.replay.kafka.KafkaConnectionFactory
import com.peoplenet.rerun.replay.kafka.KafkaMessage
import com.peoplenet.rerun.replay.kafka.RangeConsumer
import com.peoplenet.rerun.replay.kafka.RangedStream
import spock.lang.Specification

class ReplayRunnerSpec extends Specification {
    KafkaConnectionFactory mockFactory = Mock()
    ReplayJobRepo mockRepo = Mock()
    HttpForwarder mockForwarder = Mock()
    RangedStream mockStream = Mock()
    RangeConsumer mockConsumer = Mock()

    void 'job completes as expected'() {
        given: 'a replay job'
        ReplayJob job = new ReplayJob('123', goodRequest())
        ReplayRunner runner = makeRunner(job)

        when:
        runner.run()

        then:
        1 * mockRepo.updateReplayJob(*_) >> {args ->
            ReplayJob myJob = args[0]
            assert myJob.replayState == ReplayState.RUNNING
            return myJob
        }

        1 * mockConsumer.createStream(*_) >> mockStream

        1 * mockStream.beginStreaming(*_) >> {Closure args ->
            args.call(new KafkaMessage(
                    payload: 'message'.bytes,
                    offset: 0,
                    partitionId: 0,
                    topic: 'test'
            ))
        }

        1 * mockForwarder.forward(_)

        1 * mockRepo.updateReplayJob(*_) >> {args ->
            ReplayJob myJob = args[0]
            assert myJob.replayState == ReplayState.RUNNING
            assert myJob.currentOffset == 0l
            return myJob
        }

        1 * mockRepo.updateReplayJob(*_) >> {args ->
            ReplayJob myJob = args[0]
            assert myJob.replayState == ReplayState.COMPLETE
            return myJob
        }

        0 * _
    }

    void 'Forwarder throws exception'() {
        given: 'a replay job'
        ReplayJob job = new ReplayJob('123', goodRequest())
        ReplayRunner runner = makeRunner(job)

        when:
        runner.run()

        then:
        1 * mockRepo.updateReplayJob(*_) >> {args ->
            ReplayJob myJob = args[0]
            assert myJob.replayState == ReplayState.RUNNING
            return myJob
        }

        1 * mockConsumer.createStream(*_) >> mockStream

        1 * mockStream.beginStreaming(*_) >> {Closure args ->
            args.call(new KafkaMessage(
                    payload: 'message'.bytes,
                    offset: 0,
                    partitionId: 0,
                    topic: 'test'
            ))
        }

        1 * mockForwarder.forward(_) >> { throw new ForwarderException('BOOM')}

        1 * mockRepo.updateReplayJob(*_) >> {args ->
            ReplayJob myJob = args[0]
            assert myJob.errorDescription == 'BOOM'
            assert myJob.errorMsg
            assert myJob.replayState == ReplayState.ERROR
            return myJob
        }

        0 * _
    }

    ReplayRunner makeRunner(ReplayJob job) {
        return new ReplayRunner(job, mockFactory, mockRepo) {
            @Override
            protected HttpForwarder makeForwarder(HttpForwarderConfig config) {
                return mockForwarder
            }

            @Override
            protected RangeConsumer makeConsumer() {
                return mockConsumer
            }
        }
    }

    ReplayJobRequest goodRequest() {
        new ReplayJobRequest(
                mediaType: 'mediaType',
                name: 'name',
                source: new KafkaSource(
                       topic: 'test',
                       partition: 0,
                       endingOffset: 1,
                       startingOffset: 0
                ),
                target: new HttpTarget(
                       url: 'url'
                )
        )
    }
}
