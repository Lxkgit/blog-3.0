package com.blog.core.es;

import lombok.Data;
import org.dromara.easyes.annotation.IndexId;
import org.dromara.easyes.annotation.IndexName;

@Data
@IndexName("precision_customer_point_info")
public class PrecisionCustomerPointInfo {

    @IndexId
    private String id;

    /**
     * 客户号
     */
    private String customerNo;


    /**
     * 客户名称
     */
    private String customerName;

    /**
     *客户地址类型
     */
    private String comnAddrType;

    /**
     * 客户类型
     * 1 非生产经营性客户 2 生产经营性客户 4 企业客户 5 事业单位客户
     */
    private String customerType;

    /**
     * 所属机构
     */
    private String bankDeptCode;

    /**
     * 主联系人
     */
    private String bankManagerCode;

    /**
     * 个人家庭地址
     */
    private String famAddr;

    /**
     * 个人户籍地址
     */
    private String rdnCdAddr;

    /**
     * 个人经营地址
     */
    private String operAddr;

    /**
     * 个人工作地址
     */
    private String workAddr;


    /**
     * 企业注册地址
     */
    private String regiAddr;

    /**
     * 企业经营地址
     */
    private String corpOperAddr;

    /**
     * 企业单位办公地址
     */
    private String officeAddr;

    /**
     * 其它地址
     */
    private String otherAddr;

    /**
     * es中的 _index
     */
    private String index;

    /**
     * es中的 _type
     */
    private String type;

    /**
     * 所属网格
     */
    private String parentCode;

    /**
     * 网格层级
     */
    private String gridLevel;

    /**
     * 经度
     */
    private String lon;

    /**
     * 纬度
     */
    private String lat;
}
