package com.peoplenet.rerun.replay.domain

/**
 * Wrapper around the actual payload.
 */
class Message {
    final byte[] payload

    Message(byte[] payload) {
       this.payload = payload
    }

}
