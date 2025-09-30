package com.blog.core.exception;


import com.blog.core.constant.ErrorConstant;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;

@Setter
@Getter
public class ServiceException extends BaseException {

    private static final Logger logger = LoggerFactory.getLogger(ServiceException.class);

    @Serial
    private static final long serialVersionUID = 1L;

    public ServiceException(String errMsg) {
        super(errMsg);
        logger.error("服务器异常: errMsg:{}", errMsg);
    }

    public ServiceException(String code, String errMsg) {
        super(code, errMsg);
        logger.error("服务器异常: code:{} errMsg:{}", code, errMsg);
    }

    public ServiceException(ErrorConstant errorMessage) {
        super(errorMessage.getCode(), errorMessage.getDesc());
        this.setErrorMessage(errorMessage);
        logger.error("服务器异常: errorMessage:{}", errorMessage);
    }

    public ServiceException(ErrorConstant errorMessage, Object data) {
        super(errorMessage.getCode(), errorMessage.getDesc());
        this.setErrorMessage(errorMessage);
        this.setData(data);
        logger.error("服务器异常: errorMessage:{} data:{}", errorMessage, data);
    }

    public ServiceException(String code, String errMsg, Throwable cause) {
        super(code, errMsg, cause);
    }

}