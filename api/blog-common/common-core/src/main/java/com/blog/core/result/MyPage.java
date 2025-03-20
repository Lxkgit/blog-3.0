package com.blog.core.result;

import lombok.Data;

import java.util.List;

/**
 * @author: lxk
 * @time: 2021/10/12 20:24
 * @description :分页实体类
 */

@Data
public class MyPage<T> {

    private int page;

    private int size;

    private int total;

    private List<T> list;

    public MyPage(List<T> list, int page, int size, int total) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.list = list;
    }
}
