package com.blog.core.exception;


import com.blog.core.constant.ErrorMessage;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public abstract class BaseException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;
    private String errMsg;
    private String code;
    private ErrorMessage errorMessage;
    private Object args;
    private Object data;

    public BaseException(String code) {
        this.code = code;
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