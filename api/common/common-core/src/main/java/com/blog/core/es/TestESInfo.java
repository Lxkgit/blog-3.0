package com.blog.core.es;

import lombok.Data;
import org.dromara.easyes.annotation.IndexName;

@Data
@IndexName(value = "test_info")
public class TestESInfo {

    private String id;
    private String key;
    private String value;
}
