package com.peoplenet.rerun.replay.domain

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.peoplenet.rerun.replay.api.Target
import groovy.transform.CompileStatic

@CompileStatic
class HttpTarget {
    String url
    String method
    Map<String, String> queryParams
    Map<String, String> headers
    Set<Integer> ignoredErrorCodes
    Boolean batched

    Target toApi() {
       return new Target(
               url: url,
               method: method,
               queryParams: ImmutableMap.copyOf(queryParams),
               headers: ImmutableMap.copyOf(headers),
               ignoredErrorCodes: ImmutableSet.copyOf(ignoredErrorCodes),
               batched: batched
       )
    }

    static HttpTarget fromApi(Target target) {
        return new HttpTarget(
                url: target.url,
                method: target.method,
                queryParams: ImmutableMap.copyOf(target.queryParams),
                headers: ImmutableMap.copyOf(target.headers),
                ignoredErrorCodes: target.ignoredErrorCodes,
                batched: target.batched
        )
    }
}
