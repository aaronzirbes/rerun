package com.peoplenet.rerun.replay.web

import com.peoplenet.rerun.replay.api.ErrorResponse
import spock.lang.Specification

class ErrorHandlerSpec extends Specification {
    ErrorHandler handler = new ErrorHandler()

    void 'Handle generic error'() {
        given:
        Exception ex = new Exception('BOOM')

        when:
        ErrorResponse response = handler.handle(ex)

        then:
        assert response
        response.errorMessage.contains('BOOM')

    }
}
