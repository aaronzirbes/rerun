package com.peoplenet.rerun.replay.web

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import com.peoplenet.rerun.replay.api.ErrorResponse
import com.peoplenet.rerun.replay.api.FieldValidationFault
import com.peoplenet.rerun.replay.api.ValidationErrorResponse
import com.peoplenet.rerun.replay.service.JobNotFoundException
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.validation.BindException

@Slf4j
@CompileStatic
@ControllerAdvice
class ErrorHandler {

    @ExceptionHandler(Exception)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    ErrorResponse handle(Exception ex) {
        log.error 'Unexpected Exception: {}', ex
        return new ErrorResponse(errorMessage: "Unexpected Exception (${ex.message}), please check logs.")
    }

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    ValidationErrorResponse handleBindException(BindException ex) {
        log.error 'Request failed validation', ex

        Multimap<String, FieldValidationFault> faultMultiMap = ArrayListMultimap.create()
        ex.bindingResult.fieldErrors.each {
            FieldValidationFault fieldFault = new FieldValidationFault(
                    field: it.field,
                    message: it.defaultMessage,
                    rejectedValue: it.rejectedValue?.toString() ?: 'null'
            )

            faultMultiMap.put(it.field, fieldFault)
        }

        ValidationErrorResponse validationError =
                new ValidationErrorResponse(
                        message: 'Validation Error', fieldErrors: faultMultiMap.asMap())

        return validationError
    }


    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    ErrorResponse handleJobNotFound(JobNotFoundException ex) {
        log.warn 'JobNotFound: {}', ex.message

        return new ErrorResponse(errorMessage: ex.message)
    }
}
