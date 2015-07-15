package com.peoplenet.rerun.replay.forward

import com.peoplenet.rerun.replay.domain.Message
import com.peoplenet.rerun.replay.domain.MessageBatch
import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.MockWebServer
import com.squareup.okhttp.mockwebserver.RecordedRequest
import spock.lang.Specification

class HttpForwarderSpec extends Specification {

    void 'forwarder is initialized correctly'() {
        given: 'A good configuration'
        HttpForwarderConfig config = new HttpForwarderConfig('http://url')
        config.queryParams = ['qp': 'val']

        when: 'subject is instantiated'
        HttpForwarder forwarder = new HttpForwarder(config)

        then:
        assert forwarder
        forwarder.url.uri().toString() == 'http://url/?qp=val'
        assert forwarder.httpClient
    }

    void 'forwarded message is sent as json batch'() {
        given: 'A mock http server'
        MockWebServer server = new MockWebServer()
        server.enqueue(new MockResponse().setResponseCode(200))
        server.start()

        and: 'config specifying batch'
        HttpForwarderConfig config = new HttpForwarderConfig(server.getUrl('/path').toString())
        config.batchIt = true

        and: 'forwarder is instantiated'
        HttpForwarder forwarder = new HttpForwarder(config)

        and: 'a json message batch'
        Message msg = new Message('{"k":"v"}'.bytes)
        MessageBatch batch = new MessageBatch('application/json', [msg] * 3)

        when: 'batch is forwarded'
        forwarder.forward(batch)

        then:
        RecordedRequest request = server.takeRequest()
        request.getHeader('Content-Type').contains('application/json')
        request.path == '/path'
        request.getBody().readUtf8() == '[{"k":"v"},{"k":"v"},{"k":"v"}]'

        cleanup:
        server.shutdown()

    }

    void 'batchIt is ignored when not json'() {
        given: 'A mock http server'
        MockWebServer server = new MockWebServer()
        server.enqueue(new MockResponse().setResponseCode(200))
        server.enqueue((new MockResponse().setResponseCode(200)))
        server.start()

        and: 'config specifying batch'
        HttpForwarderConfig config = new HttpForwarderConfig(server.getUrl('/path').toString())
        config.batchIt = true

        and: 'forwarder is instantiated'
        HttpForwarder forwarder = new HttpForwarder(config)

        and: 'a non json message batch'
        Message msg = new Message('Not Json'.bytes)
        MessageBatch batch = new MessageBatch('text/plain', [msg] * 2)

        when: 'batch is forwarded'
        forwarder.forward(batch)

        then:
        server.getRequestCount() == 2

        [server.takeRequest(), server.takeRequest()].each {
            assert it.body.readUtf8() == 'Not Json'
            assert it.getHeader('Content-Type').contains('text/plain')
        }

        cleanup:
        server.shutdown()

    }

    void 'Ignored error codes are respected'() {
        given: 'A mock http server'
        MockWebServer server = new MockWebServer()
        server.enqueue(new MockResponse().setResponseCode(400))
        server.start()

        and: 'config specifying batch'
        HttpForwarderConfig config = new HttpForwarderConfig(server.getUrl('/path').toString())

        and: 'forwarder is instantiated'
        HttpForwarder forwarder = new HttpForwarder(config)

        and: 'a non json message batch'
        Message msg = new Message('Not Json'.bytes)
        MessageBatch batch = new MessageBatch('text/plain', [msg])

        when: 'batch is forwarded'
        forwarder.forward(batch)

        then:
        server.getRequestCount() == 1

        [server.takeRequest()].each {
            assert it.body.readUtf8() == 'Not Json'
        }

        cleanup:
        server.shutdown()
    }

    void 'bad response code causes exception'() {
        given: 'A mock http server'
        MockWebServer server = new MockWebServer()
        String response = 'BAD MOJO BRO'
        server.enqueue(new MockResponse().setResponseCode(500).setBody(response))
        server.start()

        and: 'config specifying batch'
        HttpForwarderConfig config = new HttpForwarderConfig(server.getUrl('/path').toString())

        and: 'forwarder is instantiated'
        HttpForwarder forwarder = new HttpForwarder(config)

        and: 'a non json message batch'
        Message msg = new Message('Not Json'.bytes)
        MessageBatch batch = new MessageBatch('text/plain', [msg])

        when: 'batch is forwarded'
        forwarder.forward(batch)

        then:
        def e = thrown ForwarderException
        e.message.contains response

        cleanup:
        server.shutdown()
    }


}
