package com.peoplenet.rerun.replay.forward

class ForwarderException extends RuntimeException {

    ForwarderException(String msg) {
        super(msg)
    }

    ForwarderException(String msg, Throwable t) {
        super(msg, t)
    }
}
