package com.blog.core.exception;


import com.blog.core.constant.ErrorMessage;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class ServiceException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    private String errMsg;

    public ServiceException(String code) {
        super(code);
    }

    public ServiceException(String code, String errMsg) {
        super(code, errMsg);
    }

    public ServiceException(ErrorMessage errorMessage) {
        super(errorMessage.getCode(), errorMessage.getDesc());
        this.setErrorMessage(errorMessage);
    }

    public ServiceException(ErrorMessage errorMessage, Object data) {
        super(errorMessage.getCode(), errorMessage.getDesc());
        this.setErrorMessage(errorMessage);
        this.setData(data);
    }

    public ServiceException(String code, String errMsg, Throwable cause) {
        super(code, errMsg, cause);
    }

}