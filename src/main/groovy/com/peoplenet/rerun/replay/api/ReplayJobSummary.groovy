package com.peoplenet.rerun.replay.api

import com.peoplenet.rerun.replay.domain.ReplayJob

import java.time.LocalDate

class ReplayJobSummary {

    String id
    String name
    Integer percentComplete
    LocalDate submitTime
    String replayState

    ReplayJobSummary(ReplayJob job) {
        this.id = job.id
        this.name = job.name
        this.percentComplete = job.percentComplete
        this.submitTime = job.submitTime
        this.replayState = job.replayState
    }
}
