package com.peoplenet.rerun.replay.job

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap
import com.peoplenet.rerun.replay.domain.ReplayJob
import com.peoplenet.rerun.replay.domain.ReplayJobRequest
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Repository

@CompileStatic
@Slf4j
@Repository
class ReplayJobRepo {
    private ConcurrentLinkedHashMap<String, ReplayJob> jobMap

    ReplayJobRepo() {
        jobMap = new ConcurrentLinkedHashMap.Builder()
        .concurrencyLevel(4)
        .maximumWeightedCapacity(1000)
        .build()
    }

    ReplayJob fetchReplayJob(String replayJobId) {
        return jobMap.get(replayJobId)
    }

    ReplayJob submitReplayRequest(ReplayJobRequest replayRequest) {
        String id = UUID.randomUUID().toString()

        ReplayJob replayJob = new ReplayJob(id, replayRequest)

        jobMap.put(id, replayJob)

        return replayJob
    }

    ReplayJob updateReplayJob(ReplayJob replayJob) {
        String id = replayJob.id

        jobMap.put(id, replayJob)

        return replayJob
    }

    List<ReplayJob> fetchAllJobs() {
        return jobMap.descendingMap().collect {it.value}
    }

}
