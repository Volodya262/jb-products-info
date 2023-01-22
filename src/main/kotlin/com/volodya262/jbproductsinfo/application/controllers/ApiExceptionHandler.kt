package com.volodya262.jbproductsinfo.application.controllers

import com.volodya262.jbproductsinfo.domain.BusinessLogicError
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    private val logger = LoggerFactory.getLogger(ApiExceptionHandler::class.java)

    @ExceptionHandler(BusinessLogicError::class)
    fun handleBusinessLogicError(ex: BusinessLogicError): ResponseEntity<Any> {
        logger.info("Handled BusinessLogicException", ex)

        return ResponseEntity
            .status(HttpStatus.PRECONDITION_FAILED)
            .body(ApiError(ex.errorCode, ex.readableMessage))
    }

    @ExceptionHandler(Exception::class)
    fun handleAnyException(
        ex: Exception
    ): ResponseEntity<Any> {
        logger.error("Handled Exception", ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiError.internalError("Internal error"))
    }
}

data class ApiError(
    val errorCode: String,
    val description: String
) {
    companion object {

        private const val internalErrorCode = "internalError"
        private const val invalidFormatCode = "invalidFormat"

        fun internalError(description: String?) = ApiError(
            internalErrorCode,
            description ?: "Unknown error"
        )
    }
}
