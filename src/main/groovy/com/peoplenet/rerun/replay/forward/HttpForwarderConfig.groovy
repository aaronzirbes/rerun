package com.peoplenet.rerun.replay.forward

class HttpForwarderConfig {
    String url
    String method = 'POST'
    Map<String, String> headers
    Map<String, String> queryParams
    Set<Integer> ignoreErrorCodes = [400, 422] as Set
    Boolean batchIt = false

    HttpForwarderConfig(String url) {
        this.url = url
    }

    HttpForwarderConfig(String url,
                        String method,
                        Map<String, String> headers,
                        Map<String, String> queryParams,
                        Set<Integer> ignoreErrorCodes,
                        Boolean batchIt) {

        this.url = url
        this.method = method ?: this.method
        this.headers = headers
        this.queryParams = queryParams
        this.ignoreErrorCodes = ignoreErrorCodes ?: this.ignoreErrorCodes
        this.batchIt = batchIt ?: this.batchIt
    }
}
