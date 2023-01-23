package com.volodya262.jbproductsinfo.application.controllers

import com.volodya262.jbproductsinfo.domain.BusinessLogicError
import com.volodya262.jbproductsinfo.domain.NotFoundBusinessLogicError
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    private val logger = LoggerFactory.getLogger(ApiExceptionHandler::class.java)

    @ExceptionHandler(NotFoundBusinessLogicError::class)
    fun handleNotFoundException(ex: NotFoundBusinessLogicError): ResponseEntity<Any> {
        logger.info("Handled NotFoundBusinessLogicError", ex)
        return jsonResponseEntity(ApiError(ex.errorCode, ex.readableMessage), HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(BusinessLogicError::class)
    fun handleBusinessLogicError(ex: BusinessLogicError): ResponseEntity<Any> {
        logger.info("Handled BusinessLogicException", ex)
        return jsonResponseEntity(ApiError(ex.errorCode, ex.readableMessage), HttpStatus.PRECONDITION_FAILED)
    }

    @ExceptionHandler(Exception::class)
    fun handleAnyException(
        ex: Exception
    ): ResponseEntity<Any> {
        logger.error("Handled Exception", ex)
        return jsonResponseEntity(ApiError.internalError(), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    private fun <T> jsonResponseEntity(body: T, status: HttpStatus): ResponseEntity<T> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        return ResponseEntity(body, headers, status)
    }
}

data class ApiError(
    val errorCode: String,
    val description: String
) {
    companion object {

        private const val internalErrorCode = "internalError"

        fun internalError(description: String? = null) = ApiError(
            internalErrorCode,
            description ?: "Unknown error"
        )
    }
}
