package com.blog.app.entity;

public class Article {
    private Long id;
    private String title;
    private String content;
    private String coverImage; // 封面图片URL
    private Author author;     // 作者信息
    private String createTime; // 创建时间

    // 内部类：作者信息
    public static class Author {
        private Long id;
        private String nickname; // 作者昵称

        // Getters
        public Long getId() {
            return id;
        }

        public String getNickname() {
            return nickname;
        }
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public Author getAuthor() {
        return author;
    }

    public String getCreateTime() {
        return createTime;
    }
}