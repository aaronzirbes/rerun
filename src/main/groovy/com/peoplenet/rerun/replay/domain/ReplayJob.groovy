package com.peoplenet.rerun.replay.domain

import groovy.transform.CompileStatic
import groovy.transform.ToString

import java.math.MathContext
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.ZoneOffset

@CompileStatic
@ToString
class ReplayJob {
    private static final MathContext PERCENTAGE = new MathContext(0, RoundingMode.HALF_UP)
    ReplayJobRequest replayRequest
    String id
    LocalDateTime submitTime
    ReplayState replayState
    String errorMsg
    String errorDescription
    Integer percentComplete
    Long currentOffset

    ReplayJob(String id,
              ReplayJobRequest replayRequest) {
        
        this.replayRequest = replayRequest
        this.id = id
        submitTime = LocalDateTime.now(ZoneOffset.UTC)
        replayState = ReplayState.WAITING
        percentComplete = 0
    }

    void checkpoint(Long currentOffset) {
        this.currentOffset = currentOffset

        Long completeCount = (currentOffset - replayRequest.source.startingOffset) + 1

        percentComplete =  ((completeCount / replayRequest.source.offsetCount()) * 100.0).round(PERCENTAGE).intValue()
    }
}
