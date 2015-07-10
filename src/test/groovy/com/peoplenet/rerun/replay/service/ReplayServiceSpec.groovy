package com.peoplenet.rerun.replay.service

import com.google.common.util.concurrent.ListeningExecutorService
import com.peoplenet.rerun.replay.domain.ReplayConfiguration
import com.peoplenet.rerun.replay.domain.ReplayJob
import com.peoplenet.rerun.replay.domain.ReplayJobRequest
import com.peoplenet.rerun.replay.job.ReplayJobRepo
import com.peoplenet.rerun.replay.kafka.KafkaConnectionFactory
import spock.lang.Specification

class ReplayServiceSpec extends Specification {
    ReplayJobRepo mockRepo = Mock()
    ListeningExecutorService mockExecutor = Mock()
    KafkaConnectionFactory mockFactory = Mock()
    ReplayConfiguration conf = new ReplayConfiguration(
            replayService: new ReplayConfiguration.ReplayServiceConfiguration(
                    jobThreadPoolSize: 1
            )
    )

    ReplayService service = new ReplayService(mockFactory, mockRepo, conf, mockExecutor)

    void 'replay request is submitted successfully'() {
        given: 'A replay request'
        ReplayJobRequest request = new ReplayJobRequest(
                mediaType:  'mediaType',
                name: 'name'
        )
        ReplayJob returnedJob = new ReplayJob('123', request)
        ReplayRunner capturedRunner

        when:
        String id = service.submitReplayRequest(request)

        then:
        1 * mockRepo.submitReplayRequest(request) >> returnedJob
        1 * mockExecutor.submit({capturedRunner = it})
        0 * _

        id == returnedJob.id
        assert capturedRunner
        capturedRunner.replayJob == returnedJob
    }

    void 'fetch all replay jobs'() {
        when:
        List<ReplayJob> jobs = service.fetchReplayJobs()

        then:
        1 * mockRepo.fetchAllJobs() >> [new ReplayJob('123', null)] * 3
        0 * _

        assert jobs
        jobs.size() == 3

    }

    void 'fetch an existing job by id'() {
        when:
        ReplayJob job = service.fetchReplayJob('123')

        then:
        1 * mockRepo.fetchReplayJob('123') >> new ReplayJob('123', null)
        0 * _

        assert job
    }

    void 'fetch a non existent job'() {
        when:
        ReplayJob job = service.fetchReplayJob('123')

        then:
        1 * mockRepo.fetchReplayJob('123')
        0 * _

        assert !job
        def ex = thrown JobNotFoundException
        ex.jobId == '123'
    }



}
