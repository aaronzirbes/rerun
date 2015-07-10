package com.peoplenet.rerun.replay.service

import com.google.common.util.concurrent.ListeningExecutorService
import com.google.common.util.concurrent.MoreExecutors
import com.peoplenet.rerun.replay.domain.ReplayConfiguration
import com.peoplenet.rerun.replay.domain.ReplayJob
import com.peoplenet.rerun.replay.domain.ReplayJobRequest
import com.peoplenet.rerun.replay.job.ReplayJobRepo
import com.peoplenet.rerun.replay.kafka.KafkaConnectionFactory
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util.concurrent.Executors

@CompileStatic
@Slf4j
@Service
class ReplayService {
    private final ListeningExecutorService listeningService
    private final KafkaConnectionFactory cnxnFactory
    private final ReplayJobRepo replayJobRepo
    private final ReplayConfiguration replayConfiguration

    @Autowired
    ReplayService(
            KafkaConnectionFactory cnxnFactory,
            ReplayJobRepo replayJobRepo,
            ReplayConfiguration conf) {

        this(cnxnFactory, replayJobRepo, conf,
            MoreExecutors.listeningDecorator(
                Executors.newFixedThreadPool(conf.replayService.jobThreadPoolSize)))
    }

    protected ReplayService(
            KafkaConnectionFactory cnxnFactory,
            ReplayJobRepo replayJobRepo,
            ReplayConfiguration conf,
            ListeningExecutorService listeningExecutor) {

        this.cnxnFactory = cnxnFactory
        this.replayConfiguration = conf
        this.replayJobRepo = replayJobRepo
        this.listeningService = listeningExecutor

    }

    String submitReplayRequest(ReplayJobRequest replayRequest) {
        ReplayJob replayJob = replayJobRepo.submitReplayRequest(replayRequest)

        listeningService.submit(new ReplayRunner(replayJob, cnxnFactory, replayJobRepo))

        return replayJob.id
    }

    List<ReplayJob> fetchReplayJobs() {
        return replayJobRepo.fetchAllJobs()
    }

    ReplayJob fetchReplayJob(String id) {
        ReplayJob job = replayJobRepo.fetchReplayJob(id)

        if(!job) {
            throw new JobNotFoundException(id)
        }

        return job
    }
}

