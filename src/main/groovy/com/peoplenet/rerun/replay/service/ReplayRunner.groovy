package com.peoplenet.rerun.replay.service

import com.peoplenet.rerun.replay.domain.*
import com.peoplenet.rerun.replay.forward.ForwarderException
import com.peoplenet.rerun.replay.forward.HttpForwarder
import com.peoplenet.rerun.replay.forward.HttpForwarderConfig
import com.peoplenet.rerun.replay.job.ReplayJobRepo
import com.peoplenet.rerun.replay.kafka.*
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
class ReplayRunner implements Runnable {
    private ReplayJob replayJob
    private final KafkaConnectionFactory cnxnFactory
    private final ReplayJobRepo replayJobRepo

    ReplayRunner(ReplayJob replayJob, KafkaConnectionFactory cnxnFactory, ReplayJobRepo replayJobRepo) {
        this.replayJob = replayJob
        this.cnxnFactory = cnxnFactory
        this.replayJobRepo = replayJobRepo
    }

    @Override
    void run() {
        replayJob.replayState = ReplayState.RUNNING
        replayJob = replayJobRepo.updateReplayJob(replayJob)

        RangeConsumer consumer = makeConsumer()

        OffsetRange offsetRange = new OffsetRange(
                replayJob.replayRequest.source.startingOffset,
                replayJob.replayRequest.source.endingOffset)



        HttpTarget httpTarget = replayJob.replayRequest.target

        HttpForwarderConfig httpForwarderConfig = new HttpForwarderConfig(
                httpTarget.url,
                httpTarget.method,
                httpTarget.headers,
                httpTarget.queryParams,
                httpTarget.ignoredErrorCodes,
                httpTarget.batched
        )

        HttpForwarder forwarder = makeForwarder(httpForwarderConfig)

        try {
            RangedStream stream = consumer.createStream(
                    replayJob.replayRequest.source.topic,
                    replayJob.replayRequest.source.partition.toInteger(),
                    offsetRange
            )

            stream.beginStreaming { KafkaMessage msg ->

                MessageBatch batch =
                        new MessageBatch(replayJob.replayRequest.mediaType, new Message(msg.payload))

                forwarder.forward(batch)


                replayJob.checkpoint(msg.offset)
                replayJobRepo.updateReplayJob(replayJob)
            }

            replayJob.replayState = ReplayState.COMPLETE

        } catch (Exception e) {

            switch (e) {
                case ForwarderException:
                    replayJob.errorMsg = "Forwarding of message failed."
                    replayJob.errorDescription = e.message
                    break
                default:
                    replayJob.errorMsg = e.message
            }

            replayJob.replayState = ReplayState.ERROR
            log.error "JOB: ${replayJob}"
        }

        replayJobRepo.updateReplayJob(replayJob)
    }

    protected HttpForwarder makeForwarder(HttpForwarderConfig config) {
        return new HttpForwarder(config)
    }

    protected RangeConsumer makeConsumer() {
        return new RangeConsumer(cnxnFactory)
    }
}
