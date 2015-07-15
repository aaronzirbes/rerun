package com.peoplenet.rerun

import groovy.transform.CompileStatic
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.scheduling.annotation.EnableAsync

import java.text.SimpleDateFormat

@CompileStatic
@SpringBootApplication
@EnableAsync
class RerunApplication {

    static void main(final String[] args) {
        SpringApplication.run(this, args)
    }

    @Bean
    Jackson2ObjectMapperBuilder jacksonBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder()
        builder.dateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
               .timeZone('GMT')
        return builder
    }
}
