package com.peoplenet.rerun.replay.api

import groovy.transform.CompileStatic

@CompileStatic
class ValidationErrorResponse {
    String message
    Map<String, Collection<FieldValidationFault>> fieldErrors
}
