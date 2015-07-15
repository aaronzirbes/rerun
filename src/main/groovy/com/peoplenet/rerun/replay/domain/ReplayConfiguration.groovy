package com.peoplenet.rerun.replay.domain

import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties('replay')
@CompileStatic
@Component
class ReplayConfiguration {

    ReplayServiceConfiguration replayService

    static class ReplayServiceConfiguration {
        Integer jobThreadPoolSize = 10
    }
}
