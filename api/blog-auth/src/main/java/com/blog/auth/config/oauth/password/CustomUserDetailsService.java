//package com.blog.auth.config.oauth.password;
//
//
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
///**
// * @Description
// * @Author lxk
// * @CreateTime 2026-07-03
// */
//@Service
//public class CustomUserDetailsService implements UserDetailsService {
//
////    @Autowired
////    private ISysUserService sysUserService;
//
//    /*
//     * @Description: 根据用户查询参数加载用户
//     * @author: 胡涛
//     * @mail: hutao_2017@aliyun.com
//     * @date: 2025年8月21日 下午2:00:48
//     */
//    @Override
//    public CustomUserDetail loadUserByUsername(String userName)  {
//        //这里你可以使用任何的查询方式、查询逻辑来查询你想要的用户
////        SysUser userPO = sysUserService.getById(userName);
////        if(userPO == null) {
////            throw new UsernameNotFoundException("用户不存在或用户密码错误:"+userName);
////        }
//        CustomUserDetail user = new CustomUserDetail();
////        user.setUsername(userPO.getUserName());
////        user.setPassword(userPO.getPassWord());
////        user.setEnabled(true);
////
////        //这里你可以使用任何方式来获取userInfo，然后放到CustomUserDetail中
////        UserInfo userInfo = new UserInfo();
////
////        user.setUserInfo(userInfo);
//        user.setUsername(userName);
//        user.setPassword("123456");
//
//        return user;
//    }
//}
//
