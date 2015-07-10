package com.peoplenet.rerun.replay.domain

/**
 * Contains a batch of messages to be sent at once.
 */
class MessageBatch {
    private static final String JSON_MEDIA_TYPE = 'application/json'
    final List<Message> msgBatch
    final String mediaType

    MessageBatch(String mediaType, List<Message> batch) {
        this.mediaType = mediaType
        this.msgBatch = batch
    }

    MessageBatch(String mediaType, Message message) {
        this.mediaType = mediaType
        this.msgBatch = [message]
    }

    String asJsonArray() {
        if (isJson()) {
            return '[' + msgBatch.collect { new String(it.payload) }.join(',') + ']'
        }

        return null
    }

    Boolean isJson() {
        return mediaType?.startsWith(JSON_MEDIA_TYPE)
    }
}
