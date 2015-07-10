package com.peoplenet.rerun.replay.api

import java.time.LocalDateTime

class ReplayStatus {
    String replayId
    String status
    String errorMessage
    String errorDescription
    Integer percentComplete
    LocalDateTime submitTime
}
