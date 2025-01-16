package com.blog.core.es;

import lombok.Data;

@Data
public class VisitorInfo {

    /**
     * 客户号
     */
    private String clientId;

    /**
     * 客户名称
     */
    private String clientName;

    /**
     * 处理状态
     */
    private String handlingStatus;

    /**
     * 经度
     */
    private String lon;

    /**
     * 纬度
     */
    private String lat;
}
