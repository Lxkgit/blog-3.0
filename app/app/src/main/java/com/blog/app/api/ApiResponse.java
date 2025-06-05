package com.blog.app.api;

import com.blog.app.entity.Article;

import java.util.List;

/**
 * API响应包装类
 */
public class ApiResponse {
    private int code;
    private String message;
    private Data data;

    // 内部类：数据部分
    public static class Data {
        private int total;
        private List<Article> list;

        public int getTotal() {
            return total;
        }

        public List<Article> getList() {
            return list;
        }
    }

    // Getters
    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Data getData() {
        return data;
    }
}
