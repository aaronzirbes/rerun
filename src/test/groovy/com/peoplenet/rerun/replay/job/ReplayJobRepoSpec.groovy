package com.peoplenet.rerun.replay.job

import com.peoplenet.rerun.replay.domain.ReplayJob
import com.peoplenet.rerun.replay.domain.ReplayJobRequest
import spock.lang.Specification

class ReplayJobRepoSpec extends Specification {
    ReplayJobRepo repo = new ReplayJobRepo()

    void 'submit saves request'() {
        given: 'a good request'
        ReplayJobRequest request = new ReplayJobRequest(
                mediaType: 'mediatype',
                name: 'name',
        )

        when:
        ReplayJob job = repo.submitReplayRequest(request)

        then:
        assert job
        assert job.id

        when:
        ReplayJob savedJob = repo.fetchReplayJob(job.id)

        then:
        assert savedJob
        savedJob.id == job.id
    }

    void 'update updates request'() {
        given: 'an existing job'
        ReplayJobRequest request = new ReplayJobRequest(
                mediaType: 'mediatype',
                name: 'name',
        )
        ReplayJob job = repo.submitReplayRequest(request)

        when:
        job.currentOffset = 0
        ReplayJob updated = repo.updateReplayJob(job)

        then:
        updated.currentOffset == 0
    }

    void 'fetch all returns all jobs in order'() {
        given: 'several saved jobs'
        (0..2).each {it ->
            ReplayJobRequest request = new ReplayJobRequest(
                    mediaType: 'mediatype',
                    name: "name-${it}"
            )

            repo.submitReplayRequest(request)
        }

        when:
        List<ReplayJob> jobList = repo.fetchAllJobs()

        then:
        jobList.size() == 3
        jobList[0].replayRequest.name == 'name-2'
        jobList[1].replayRequest.name == 'name-1'
        jobList[2].replayRequest.name == 'name-0'
    }
}
