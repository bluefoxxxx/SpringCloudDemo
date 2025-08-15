package org.example.orderservice.handler;


import convention.exception.AbstractException;
import convention.result.Result;
import convention.result.Results;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 拦截处理自定义的异常 {@link AbstractException}
     */
    @ExceptionHandler(value = AbstractException.class)
    public Result<Void> abstractException(AbstractException ex) {
        log.error("AbstractException: [code: {}, message: {}]", ex.getErrorCode(), ex.getErrorMessage(), ex);
        return Results.failure(ex);
    }

    /**
     * 拦截处理未被定义的异常
     */
    @ExceptionHandler(value = Exception.class)
    public Result<Void> handleException(Exception ex) {
        log.error("Unhandled Exception", ex);
        // 对于未知的系统异常，返回一个通用的“系统执行出错”响应
        return Results.failure();
    }
}
