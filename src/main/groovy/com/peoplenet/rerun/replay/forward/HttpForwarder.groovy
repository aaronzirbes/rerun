package com.peoplenet.rerun.replay.forward

import com.peoplenet.rerun.replay.domain.Message
import com.peoplenet.rerun.replay.domain.MessageBatch
import com.squareup.okhttp.HttpUrl
import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import com.squareup.okhttp.Response
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
class HttpForwarder {


    private HttpUrl url
    private OkHttpClient httpClient
    private HttpForwarderConfig config

    HttpForwarder(HttpForwarderConfig config) {
        this.config = config

        HttpUrl.Builder urlBuilder = HttpUrl.parse(config.url).newBuilder()
        config.queryParams?.each {
            urlBuilder.addQueryParameter(it.key, it.value)
        }

        this.url = urlBuilder.build()

        this.httpClient = new OkHttpClient()
    }

    void forward(MessageBatch batch) throws ForwarderException {
        MediaType mediaType = MediaType.parse(batch.mediaType)

        if(config.batchIt && batch.isJson()) {
            String json = batch.asJsonArray()
            Message msg = new Message(json.bytes)
            forwardMessage(mediaType, msg)
        } else {
            for (msg in batch.msgBatch) {
                forwardMessage(mediaType, msg)
            }
        }
    }

    private void forwardMessage(MediaType mediaType, Message message) throws ForwarderException {

        RequestBody body = RequestBody.create(mediaType, message.payload)

        Request.Builder builder = new Request.Builder()
                .method(config.method, body)
                .url(url)

        config.headers?.each { String k, String v ->
            builder.header(k, v)
        }

        Request request = builder.build()
        Response response

        try {
            response = httpClient.newCall(request).execute()
        } catch(Exception e) {
            String msg = "FORWARD EXCEPTION: ${request.method()} ${request.urlString()}, RESPONSE: ${e.message}"
            log.error msg, e

            throw new ForwarderException(msg, e)
        }

        if (response.isSuccessful() || config.ignoreErrorCodes.contains(response.code())) {
            log.info "FORWARD: ${request.method()} ${request.urlString()}, RESPONSE: ${response.code()}"
        } else {
            String msg = "FORWARD: ${request.method()} ${request.urlString()}, RESPONSE: ${response.code()}"
            String responseBody

            try {
                responseBody = response.body().string()
            } catch(IOException e) {
                log.error 'Unable to get response body from HTTP request', e
            }

            if (body) {
               msg = "${msg}, BODY: [${responseBody}]"
            }

            log.error msg

            throw new ForwarderException(msg)
        }

    }
}

