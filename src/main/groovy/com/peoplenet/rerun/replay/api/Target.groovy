package com.peoplenet.rerun.replay.api

import javax.validation.constraints.NotNull

class Target {

    @NotNull
    String url

    String method
    Map<String, String> queryParams
    Map<String, String> headers
    Set<Integer> ignoredErrorCodes
    Boolean batched
}
