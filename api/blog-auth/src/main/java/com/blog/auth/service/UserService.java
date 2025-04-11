package com.blog.auth.service;

import com.blog.core.domain.auth.vo.UserVo;
import com.blog.core.result.MyPage;

/**
 * @author lxk
 * @description 用户接口服务
 * @date 2025/03/24
 */

public interface UserService {

    UserVo selectUserById(Integer userId);
    UserVo selectUserByUsername(String username);
    void updateUser(UserVo userVo, Integer perFlag);
    MyPage<UserVo> selectUserByPage(UserVo userVo);
}
