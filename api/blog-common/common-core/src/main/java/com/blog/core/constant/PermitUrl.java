package com.blog.core.constant;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @Description 白名单接口
 * @Author lxk
 * @CreateTime 2025-03-03
 */

public class PermitUrl {

    private static final String[] basePermitUrl = {};

    private static final String[] authPermitUrl = {
            "/user/select/id"
    };

    private static final String[] filePermitUrl = {
            "/files/**",
            "/img/**",
            "/client/**",
            "/result",
            "/mq/**",
            "/data",
            "/setting/id",
            "/upload/images/**"
    };

    private static final String[] contentPermitUrl = {
            "/article/list",
            "/article/id",
            "/article/type/tree",
            "/article/type/node",
            "/doc/catalog/tree",
            "/doc/content/id",
            "/doc/catalog/id",
            "/doc/content/user",
            "/article/type/id",
            "/article/label/list"};

    /**
     * 需要放开权限的url
     *
     * @param model 模块
     * @return 自定义的url和监控中心需要访问的url集合
     */
    public static String[] permitAllUrl(String model) {
        Set<String> set = new HashSet<>();
        Collections.addAll(set, basePermitUrl);
        switch (model) {
            case "auth" -> Collections.addAll(set, authPermitUrl);
            case "file" -> Collections.addAll(set, filePermitUrl);
            case "content" -> Collections.addAll(set, contentPermitUrl);
        }
        return set.toArray(new String[0]);
    }
}
