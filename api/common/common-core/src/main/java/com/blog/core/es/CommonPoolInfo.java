package com.blog.core.es;

import lombok.Data;
import org.dromara.easyes.annotation.IndexId;
import org.dromara.easyes.annotation.IndexName;

@Data
@IndexName(value = "gmms_grid_allocated_cust_list")
public class CommonPoolInfo {

    @IndexId
    private String id;

    private String custId;
    private String custName;
    private String parentCode;

    private String lon;
    private String lat;
}
