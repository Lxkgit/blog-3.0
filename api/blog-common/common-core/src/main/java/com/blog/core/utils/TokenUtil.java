package com.blog.core.utils;


import com.blog.core.entity.auth.BlogUser;

/**
 * @Author: lxk
 * @date 2022/6/13 15:01
 * @description: token解析
 */

public class TokenUtil {

    public static BlogUser getUserInfo(String authorization){
        BlogUser blogUser = new BlogUser();
        String userId = (authorization.split(" ")[1]).split(":")[0];
        blogUser.setId(Integer.parseInt(userId));
        return blogUser;
    }
}