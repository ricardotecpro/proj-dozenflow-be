package com.dozenflow.be.exception;

import com.dozenflow.be.attachment.InvalidAttachmentException;
import com.dozenflow.be.boardsettings.InvalidBackgroundImageException;
import com.dozenflow.be.list.LastActiveListException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles custom business logic exceptions, like when a specific entity is not found.
     *
     * @param ex The exception.
     * @return A ResponseEntity with a NOT_FOUND status and a structured ApiError.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex) {
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, ex.getMessage(), null);
        return buildResponseEntity(apiError, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles attachment upload validation failures (empty file, too large, unsupported type).
     */
    @ExceptionHandler(InvalidAttachmentException.class)
    protected ResponseEntity<Object> handleInvalidAttachment(InvalidAttachmentException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
        return buildResponseEntity(apiError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles background image upload validation failures (empty file, too large, or an unsupported type).
     */
    @ExceptionHandler(InvalidBackgroundImageException.class)
    protected ResponseEntity<Object> handleInvalidBackgroundImage(InvalidBackgroundImageException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
        return buildResponseEntity(apiError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles attempts to archive/delete the last remaining active list on the board.
     */
    @ExceptionHandler(LastActiveListException.class)
    protected ResponseEntity<Object> handleLastActiveList(LastActiveListException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
        return buildResponseEntity(apiError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Overrides the default (ResponseEntityExceptionHandler already handles this exception type,
     * so this must be an override, not a separate @ExceptionHandler, to avoid an ambiguous mapping)
     * handler for uploads that exceed spring.servlet.multipart.max-file-size/max-request-size —
     * thrown by Spring's multipart resolver before the controller (and our own size check) ever runs.
     */
    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "File exceeds the 5MB size limit", null);
        return buildResponseEntity(apiError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Overrides the default handler for bean validation errors to return our custom ApiError structure.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<String> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Validation Error", validationErrors);
        return buildResponseEntity(apiError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Overrides the default handler for NoResourceFoundException to provide a consistent API error response.
     */
    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, "Resource not found at path: " + ex.getResourcePath(), null);
        return buildResponseEntity(apiError, HttpStatus.NOT_FOUND);
    }

    /**
     * A fallback handler for any other unhandled exceptions. This is a safety net.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(Exception ex, WebRequest request) {
        log.error("An unexpected internal error occurred", ex);
        String message = "An unexpected error occurred. Please contact support.";
        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, message, null);
        return buildResponseEntity(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError apiError, HttpStatus status) {
        return new ResponseEntity<>(apiError, status);
    }
}