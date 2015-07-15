package com.peoplenet.rerun.replay.kafka

import groovy.transform.CompileStatic

@CompileStatic
class OffsetRange {
    Long start
    Long end

    OffsetRange(Long one) {
        start = one
        end = one
    }

    OffsetRange(Long start, Long end) {
        this.start = start
        this.end = end
    }

    boolean before(Long offset) {
        offset < start
    }

    boolean after(Long offset) {
        offset > end
    }

    boolean includesOffset(Long offset) {
        start <= offset && end >= offset
    }
}
