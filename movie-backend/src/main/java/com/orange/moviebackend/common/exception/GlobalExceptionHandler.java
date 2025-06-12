package com.orange.moviebackend.common.exception;

import com.orange.moviebackend.common.constant.HttpStatus;
import com.orange.moviebackend.common.response.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;

@Slf4j
@RestController
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle validation failure exceptions and return error messages to the frontend
     *
     * @return Error response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        log.error("Validation failed: {}", e.getMessage(), e);
        BindingResult bindingResult = e.getBindingResult();
        String msg = bindingResult.getAllErrors().get(0).getDefaultMessage();
        return R.error(msg);
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R sqlIntegrityConstraintViolationExceptionHandler(SQLIntegrityConstraintViolationException e) {
        log.error("SQL integrity constraint violation: {}", e.getMessage(), e);
        return R.error("Illegal insert or update operation");
    }

    @ExceptionHandler(DataNotFoundException.class)
    public R dataNotFoundExceptionHandler(DataNotFoundException e) {
        log.warn("Data not found: {}", e.getMessage());
        return R.error(e.getMessage());
    }

    @ExceptionHandler(NoSuchMethodException.class)
    public R noSuchMethodExceptionHandler(NoSuchMethodException e) {
        log.warn("No such method found: {}", e.getMessage());
        return R.error("Sorry, an internal server error occurred");
    }

    @ExceptionHandler(IllegalAccessException.class)
    public R illegalAccessExceptionHandler(IllegalAccessException e) {
        log.warn("Illegal access: {}", e.getMessage());
        return R.error("Sorry, an internal server error occurred");
    }

    @ExceptionHandler(IOException.class)
    public R IOExceptionHandler(IOException e) {
        log.warn("IO Exception: {}", e.getMessage());
        return R.error("File information error: " + e.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public R authenticationExceptionHandler(AuthenticationException e) {
        log.warn("Authentication failed: {}", e.getMessage());
        return R.error(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}