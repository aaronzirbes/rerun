package com.peoplenet.rerun.replay.kafka

import groovy.transform.CompileStatic
import org.hibernate.validator.constraints.NotEmpty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@CompileStatic
@Component
@ConfigurationProperties('kafka')
class KafkaConfiguration {

    @NotEmpty
    List<String> bootstrapBrokers

    String clientId
}
