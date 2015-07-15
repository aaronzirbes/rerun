package com.peoplenet.rerun.replay.kafka

import spock.lang.Specification

class OffsetRangeSpec extends Specification {

    void 'Test an offset of one' () {
        given: 'a single offset'
        OffsetRange range = new OffsetRange(5)

        expect:
        range.before(4)
        range.after(6)
        !range.before(5)
        !range.after(5)
        range.includesOffset(5)
        !range.includesOffset(6)
    }

    void 'Test a standard range'() {
        given: 'a range between 2 numbers'
        OffsetRange range = new OffsetRange(0, 10)

        expect:
        range.before(-1)
        range.after(11)
        range.includesOffset(0)
        range.includesOffset(10)
        !range.includesOffset(11)
        !range.includesOffset(-1)
    }

}
