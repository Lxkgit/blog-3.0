package com.blog.core.exception;


import com.blog.core.constant.ErrorConstant;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class ServiceException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ServiceException(String errMsg) {
        super(errMsg);
    }

    public ServiceException(String code, String errMsg) {
        super(code, errMsg);
    }

    public ServiceException(ErrorConstant errorMessage) {
        super(errorMessage.getCode(), errorMessage.getDesc());
        this.setErrorMessage(errorMessage);
    }

    public ServiceException(ErrorConstant errorMessage, Object data) {
        super(errorMessage.getCode(), errorMessage.getDesc());
        this.setErrorMessage(errorMessage);
        this.setData(data);
    }

    public ServiceException(String code, String errMsg, Throwable cause) {
        super(code, errMsg, cause);
    }

}