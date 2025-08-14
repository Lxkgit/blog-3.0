package com.blog.core.exception;


import com.blog.core.constant.ErrorConstant;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public abstract class BaseException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;
    private String errMsg;
    private String code;
    private ErrorConstant errorMessage;
    private Object args;
    private Object data;

    public BaseException(String errMsg) {
        this.errMsg = errMsg;
    }

    public BaseException(String code, String errMsg) {
        super(errMsg);
        this.code = code;
        this.errMsg = errMsg;
    }

    public BaseException(String code, String errMsg, Throwable cause) {
        super(errMsg, cause);
        this.code = code;
        this.errMsg = errMsg;
    }

}