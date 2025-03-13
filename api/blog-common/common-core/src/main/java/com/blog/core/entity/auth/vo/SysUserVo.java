package com.blog.core.entity.auth.vo;


import com.blog.core.entity.auth.BlogUser;
import com.blog.core.entity.auth.SysRole;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

/**
 * @Author: lxk
 * @date 2022/12/19 19:44
 * @description: 用户Vo
 */

@Getter
@Setter
public class SysUserVo extends BlogUser {

    private static final long serialVersionUID = 9105880407113575886L;

    private Set<SysRole> sysRole;
    private List<Integer> roleIds;
}
