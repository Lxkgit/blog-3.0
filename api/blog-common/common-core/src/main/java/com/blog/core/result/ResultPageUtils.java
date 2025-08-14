package com.blog.core.result;

import java.util.List;

/**
 * @author: lxk
 * @time: 2021/10/12 20:43
 * @description :分页工具
 */

public class ResultPageUtils {

    public static <T> ResultPage<T> pageUtil(List<T> list, int page, int size, long total) {
        return new ResultPage<>(list, page, size, total);
    }
}
