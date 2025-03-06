package com.blog.auth.service;


import com.blog.core.entity.auth.BlogUser;
import com.blog.core.entity.auth.vo.BlogUserVo;
import com.blog.core.entity.auth.vo.SysUserVo;
import com.blog.core.utils.MyPage;

public interface IUserService {


    BlogUser selectUserById(Integer userId);
    String getUserVerifyCode(String email);
    String registerUser(BlogUserVo blogUserVo);
    BlogUser selectUserByUsername(String username);
    MyPage<SysUserVo> selectUserByPage(int page, int size);

    /**
     * 修改用户信息
     * @param sysUserVo 用户信息
     * @param perFlag 是否修改用户权限 1修改 0不修改
     */
    void updateUser(SysUserVo sysUserVo, Integer perFlag);
}
