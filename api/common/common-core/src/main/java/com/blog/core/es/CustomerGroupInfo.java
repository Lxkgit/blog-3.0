package com.blog.core.es;

import lombok.Data;
import org.dromara.easyes.annotation.IndexId;
import org.dromara.easyes.annotation.IndexName;

@Data
@IndexName("customer_group_info")
public class CustomerGroupInfo {

    @IndexId
    private String id;

    private String customGroupCode;
    private String customGroupName;
    private String ownerId;
    private String ownerName;
    private String creatorId;
    private String creatorName;
    private String customType;
    private String customTypeName;
    private String orgId;
    private String orgName;
}
