package com.zuche.exception;

import com.zuche.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数验证失败: {}", message);
        return Result.error(400, message);
    }
    
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数绑定失败: {}", message);
        return Result.error(400, message);
    }
    
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        // 提供更具体的错误信息
        String errorMessage = "系统异常，请稍后重试";
        if (e instanceof NullPointerException) {
            errorMessage = "系统遇到了空指针错误，请稍后重试";
        } else if (e instanceof IllegalArgumentException) {
            errorMessage = "参数错误: " + e.getMessage();
        } else if (e instanceof org.springframework.jdbc.BadSqlGrammarException) {
            errorMessage = "数据库查询错误，请稍后重试";
        } else if (e instanceof org.springframework.dao.DataAccessException) {
            errorMessage = "数据库访问错误，请稍后重试";
        }
        return Result.error(errorMessage);
    }
}
