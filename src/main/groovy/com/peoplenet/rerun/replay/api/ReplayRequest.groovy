package com.peoplenet.rerun.replay.api

import javax.validation.constraints.NotNull

class ReplayRequest {
     @NotNull
     String name

     @NotNull
     String mediaType

     @NotNull
     Source source

     @NotNull
     Target target
}
