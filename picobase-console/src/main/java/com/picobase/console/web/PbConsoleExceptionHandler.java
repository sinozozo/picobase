package com.picobase.console.web;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.picobase.PbManager;
import com.picobase.console.error.BadRequestException;
import com.picobase.console.model.dto.FailureResult;
import com.picobase.exception.PbException;
import com.picobase.validator.Err;
import com.picobase.validator.Errors;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;


//@RequestMapping("/api") //todo 这里只拦截Pocket的请求，需要修改路径
@Order(0)
@RestControllerAdvice
public class PbConsoleExceptionHandler {

    /**
     * 处 理 form data 方 式 调 用 接 口 校 验 失 败 抛 出 的 异 常
     *//*
    @ExceptionHandler(BindException.class)
    public FailureResult bindExceptionHandler(BindException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        List<String> collect = fieldErrors.stream().map(o -> o.getDefaultMessage()).collect(Collectors.toList());
        return new FailureResult(400, "Bad Request", collect);
    }

    *//**
     * 处 理 json 请 求 体 调 用 接 口 校 验 失 败 抛 出 的 异 常
     *//*
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<FailureResult> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();

        Map<String, Map<String, String>> errors = new HashMap<>();

        fieldErrors.stream().forEach(o -> {
            Map<String, String> error = new HashMap<>();
            error.put("code", o.getField());
            error.put("message", o.getDefaultMessage());
            errors.put(o.getField(), error);
        });

        return ResponseEntity.status(400).body(FailureResult.builder()
                .code(400)
                .message("Failed to validate.")
                .data(errors)
                .build());
    }


    */

    /**
     * 处 理 单 个 参 数 校 验 失 败 抛 出 的 异 常
     *//*
    @ExceptionHandler(ConstraintViolationException.class)
    public FailureResult constraintViolationExceptionHandler(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        List<String> collect = constraintViolations.stream().map(o -> o.getMessage()).collect(Collectors.toList());
        return new FailureResult(400, "Bad Request", collect);
    }*/
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity badRequestException(BadRequestException e) {
        Map<String, Object> errors = null;
        if (null != e.getErrors()) {
            errors = buildResponseStruct(e.getErrors());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new FailureResult().setCode(HttpStatus.BAD_REQUEST.value()).setMessage(e.getMessage()).setData(errors));
    }


    private static Map<String, Object> buildResponseStruct(Errors errors) {
        Map<String, Object> result = new HashMap<>();

        Map<String, Err> errorMap = errors.getErrorMap();

        errorMap.forEach((fieldName, value) -> {
            if (value instanceof Errors v) {
                result.put(fieldName, buildResponseStruct(v));
            } else {
                // ErrorObject
                result.put(fieldName, value);
            }
        });
        return result;
    }

    @ExceptionHandler(PbException.class)
    public ResponseEntity pbException(PbException e) {
        PbManager.getLog().error("PbException: {}", ExceptionUtil.getRootCause(e));
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new FailureResult().setCode(HttpStatus.BAD_REQUEST.value()).setMessage(e.getMessage()).setData(""));
    }
/*

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity unauthorizedException(UnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(FailureResult.builder().code(HttpStatus.UNAUTHORIZED.value()).message(e.getMessage()).data(e.getData()).build());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity forbiddenException(ForbiddenException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(FailureResult.builder().code(HttpStatus.FORBIDDEN.value()).message(e.getMessage()).data(e.getData()).build());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity notFoundException(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(FailureResult.builder().code(HttpStatus.NOT_FOUND.value()).message(e.getMessage()).build());
    }
*/

}
