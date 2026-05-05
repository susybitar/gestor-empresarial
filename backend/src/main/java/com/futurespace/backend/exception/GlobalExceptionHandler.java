package com.futurespace.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manejador global de excepciones de la API.
 *
 * Centraliza la traducción de excepciones a respuestas HTTP con un
 * formato JSON uniforme. Evita exponer detalles internos al cliente
 * (no se filtran trazas ni mensajes técnicos en errores 500).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Errores de reglas de negocio del dominio. */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusinessException(BusinessException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /** Errores de validación de Bean Validation sobre el cuerpo de la petición. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        String message = "Existen errores de validación en el formulario";
        return buildResponse(HttpStatus.BAD_REQUEST, message, request, fieldErrors);
    }

    /** JSON malformado o tipo no parseable en el cuerpo de la petición. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleNotReadable(HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es válido", request);
    }

    /** Tipo incorrecto en una path variable o en un parámetro de query. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST,
                "Parámetro inválido: " + ex.getName(), request);
    }

    /** Violación de restricciones de integridad (NIF duplicado, FKs, etc.). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT,
                "La operación viola una restricción de integridad de datos", request);
    }

    /**
     * Cualquier otro error no contemplado se trata como interno del servidor.
     *
     * Se genera un identificador corto que se incluye tanto en el log
     * como en la respuesta al cliente. Permite correlacionar el error
     * que ve el usuario con la traza completa que queda en servidor.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex,
            HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString().substring(0, 8);
        log.error("Error no controlado [errorId={}] en {}", errorId, request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor (ref: " + errorId + ")", request);
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, String message,
            HttpServletRequest request) {
        return buildResponse(status, message, request, null);
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, String message,
            HttpServletRequest request, Map<String, String> errors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        if (errors != null) {
            body.put("errors", errors);
        }
        body.put("path", request.getRequestURI());
        return new ResponseEntity<>(body, status);
    }
}
