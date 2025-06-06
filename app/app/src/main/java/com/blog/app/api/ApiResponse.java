package com.blog.app.api;

import com.blog.app.entity.Article;

import java.util.List;

/**
 * API响应包装类
 */
public class ApiResponse {
    private String code; // 改为字符串类型
    private String message;
    private ResultData result; // 改为result字段
    private boolean success; // 新增字段

    // Getters and setters

    public static class ResultData {
        private int page;
        private int size;
        private int total;
        private List<Article> list;

        // Getters and setters

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public List<Article> getList() {
            return list;
        }

        public void setList(List<Article> list) {
            this.list = list;
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ResultData getResult() {
        return result;
    }

    public void setResult(ResultData result) {
        this.result = result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
