package com.peoplenet.rerun.replay.exception

import groovy.transform.CompileStatic

@CompileStatic
class PublishException extends RuntimeException {
    PublishException(String msg) {
        super(msg)
    }

    PublishException(String msg, Throwable t) {
        super(msg, t)
    }
}
