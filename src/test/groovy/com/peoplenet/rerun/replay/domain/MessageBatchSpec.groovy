package com.peoplenet.rerun.replay.domain

import spock.lang.Specification

class MessageBatchSpec extends Specification {

    void 'asJsonArray produces valid JSON'() {
        given: 'valid input json'
        List<String> validJsons = ['{"key":"value"}'] * 3

        and: 'a good message batch'
        MessageBatch batch = new MessageBatch('application/json',
                validJsons.collect {new Message(it.bytes)})

        when:
        String json = batch.asJsonArray()

        then:
        json == '[{"key":"value"},{"key":"value"},{"key":"value"}]'
    }

    void 'asJsonArray returns null when payload is not json'() {
        given: 'valid non-json input'
        List<String> notJson = ['1,2,3'] * 3

        and: 'a good message batch that is not json'
        MessageBatch batch = new MessageBatch('application/csv',
            notJson.collect { new Message(it.bytes)})

        when:
        String json = batch.asJsonArray()

        then:
        !json
    }

}
