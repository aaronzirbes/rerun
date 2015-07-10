package com.peoplenet.rerun.replay.api

import javax.validation.constraints.NotNull

class Source {

    @NotNull
    String topic

    @NotNull
    String partition

    @NotNull
    Long startingOffset

    @NotNull
    Long endingOffset
}
