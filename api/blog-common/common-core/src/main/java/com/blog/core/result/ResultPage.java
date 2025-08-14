package com.blog.core.result;

import lombok.Data;

import java.util.List;

/**
 * @author: lxk
 * @time: 2021/10/12 20:24
 * @description :分页实体类
 */

@Data
public class ResultPage<T> {

    private int page;

    private int size;

    private long total;

    private List<T> list;

    public ResultPage(List<T> list, int page, int size, long total) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.list = list;
    }
}
