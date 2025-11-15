package com.girsang.server.config

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .badRequest()
            .body(mapOf("error" to ex.message.orEmpty()))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {

        val errors = ex.bindingResult.fieldErrors.associate {
            it.field to (it.defaultMessage ?: "Invalid value")
        }

        return ResponseEntity
            .badRequest()
            .body(
                mapOf(
                    "status" to 400,
                    "errors" to errors
                )
            )
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException::class)
    fun handleConstraintViolation(ex: jakarta.validation.ConstraintViolationException): ResponseEntity<Map<String, Any>> {

        val errors = ex.constraintViolations.associate {
            (it.propertyPath.lastOrNull()?.name ?: "unknown") to it.message
        }

        return ResponseEntity
            .badRequest()
            .body(
                mapOf(
                    "status" to 400,
                    "errors" to errors
                )
            )
    }

}
