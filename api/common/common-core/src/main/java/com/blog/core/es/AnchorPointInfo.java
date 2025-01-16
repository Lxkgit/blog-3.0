package com.blog.core.es;

import lombok.Data;
import org.dromara.easyes.annotation.IndexField;
import org.dromara.easyes.annotation.IndexId;
import org.dromara.easyes.annotation.IndexName;
import org.dromara.easyes.annotation.rely.FieldType;

import java.util.List;

@Data
@IndexName(value = "gmms_ps_cust_anchor_point")
public class AnchorPointInfo {

    @IndexId
    private String id;

    /**
     * 楼宇名称
     */
    @IndexField("building_name")
    private String buildingName;

    @IndexField("parent_name")
    private String parentName;

    @IndexField("customer_address")
    private String customerAddress;

    @IndexField("addr_type")
    private String addrType;

    /**
     * 经度
     */
    @IndexField("lon")
    private String lon;

    @IndexField("comn_addr_flag")
    private String comnAddrFlag;

    @IndexField("grid_name")
    private String gridName;

    @IndexField("grid_code")
    private String gridCode;

    @IndexField("dept_code_ancestors")
    private String deptCodeAncestors;

    @IndexField("bank_manager_code")
    private String bankManagerCode;

    @IndexField("grid_manager_code")
    private String gridManagerCode;

    @IndexField("customer_attribute")
    private List<String> customerAttribute;

    @IndexField("update_time")
    private String updateTime;

    @IndexField("location")
    private String location;

    @IndexField("parent_code")
    private String parentCode;

    @IndexField("customer_name")
    private String customerName;

    @IndexField("customer_id")
    private String customerId;

    /**
     * 纬度
     */
    @IndexField("lat")
    private String lat;

    @IndexField("grid_dept_code")
    private String gridDeptCode;

    @IndexField("grid_level")
    private String gridLevel;

    @IndexField("is_desensitize")
    private String isDesensitize;

    /**
     * 是否在规划地内
     */
    @IndexField("is_planned_area_flag")
    private String isPlannedAreaFlag;

    /**
     * 支行规划地（0否1是）
     */
    @IndexField("is_subbarnch_planlands_flag")
    private String isSubbarnchPlanlandsFlag;

    /**
     * 客户经理规划地 0否1是
     */
    @IndexField("is_manager_planlands_flag")
    private String isManagerPlanlandsFlag;

    /**
     * es查询的唯一id（修改用）
     */
    @IndexField("unique_id")
    private String uniqueId;

    /**
     * es 查询中的type
     */
    @IndexField("es_type")
    private String esType;

    /**
     * 用户五六级网格汇总
     */
    @IndexField("grid_code_ancestors")
    private String gridCodeAncestors;

    /**
     * 楼宇id
     */
    @IndexField("building_id")
    private String buildingId;

    /**
     * 楼宇经度
     */
    @IndexField("build_lon")
    private String buildLon;

    /**
     * 楼宇纬度
     */
    @IndexField("building_lat")
    private String buildingLat;

    /**
     * 楼宇类型
     */
    @IndexField("building_type")
    private String buildingType;

    /**
     * 判断是客户锚点还是楼宇锚点
     * 0 客户锚点
     * 1 楼宇锚点
     */
    @IndexField("building_flag")
    private String buildingFlag;

    /**
     * 判断是否是重复点位（经纬度完全相同）
     * 1 重复
     * 0 不重复
     */
    @IndexField("repeat_flag")
    private String repeatFlag;

    /**
     *  重复点位的数量
     */
    @IndexField("repeat_count")
    private String repeatCount;

    /**
     * 重复点位组级id
     */
    @IndexField("repeat_primary_key")
    private String repeatPrimaryKey;

    // 客户管理导出用
    /**
     * 主管机构
     */
//    @Excel(name = "主管机构")
    @IndexField("bank_dept_name")
    private String bankDeptName;

    /**
     * 主管机构编号
     */
//    @Excel(name = "主管机构编号")
    @IndexField("bank_dept_code")
    private String bankDeptCode;

    /**
     * 主联系人
     */
//    @Excel(name = "主联系人")
    @IndexField("bank_manage_name")
    private String bankManageName;

    /**
     * 主联系人编号
     */
//    @Excel(name = "主联系人编号")
    @IndexField("bank_manage_code")
    private String bankManageCode;

    /**
     * 个人工作地址
     */
//    @Excel(name = "个人工作地址")
    @IndexField("work_addr")
    private String workAddr;

    /**
     * 个人家庭地址
     */
//    @Excel(name = "个人家庭地址")
    @IndexField("fam_addr")
    private String famAddr;

    /**
     * 个人户籍地址
     */
//    @Excel(name = "个人户籍地址")
    @IndexField("rdn_cd_addr")
    private String rdnCdAddr;

    /**
     * 个人经营地址
     */
//    @Excel(name = "个人经营地址")
    @IndexField("oper_addr")
    private String operAddr;

    /**
     * 企业注册地址
     */
//    @Excel(name = "企业注册地址")
    @IndexField("regi_addr")
    private String regiAddr;

    /**
     * 企业经营地址
     */
//    @Excel(name = "企业经营地址")
    @IndexField("corp_oper_addr")
    private String corpOperAddr;

    /**
     * 企业单位地址
     */
//    @Excel(name = "企业单位地址")
    @IndexField("office_addr")
    private String officeAddr;

    /**
     * 客户类型 1个人客户 2企业客户 3政府行政单位
     */
////    @Excel(name = "客户类型", readConverterExp = "1: 个人客户 2: 企业客户 3: 政府行政单位")
    @IndexField("customer_type")
    private Integer customerType;

    @IndexField("credit_flag")
    private String creditFlag;
}
