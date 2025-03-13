//package com.blog.auth.service.impl;
//
//
//import com.blog.auth.service.IUserService;
//import com.blog.auth.service.MenuService;
//import com.blog.auth.service.SysRoleService;
//import com.blog.core.entity.auth.BlogUser;
//import com.blog.core.entity.auth.SysRole;
//import jakarta.annotation.Resource;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.BeanUtils;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.util.Collections;
//import java.util.Date;
//import java.util.Objects;
//import java.util.Set;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//
///**
// * @Author: lxk
// * @date 2022/6/6 16:50
// * @description:
// */
//
//@Service
//public class UserServiceImpl implements IUserService {
//
//    @Resource
//    private SysUserDAO sysUserDAO;
//
//    @Resource
//    private SysRoleService sysRoleService;
//
//    @Resource
//    private MenuService sysPermissionService;
//
//    @Resource
//    private BCryptPasswordEncoder passwordEncoder;
//
//    @Resource
//    private RedisTemplate<String, Object> redisTemplate;
//
//
//    @Override
//    public BlogUser selectUserById(Integer userId) {
//        return sysUserDAO.selectUserById(userId);
//    }
//
//    @Override
//    public String getUserVerifyCode(String email) {
//        if (Objects.equals(redisTemplate.hasKey(email),Boolean.TRUE)) {
//            return ErrorMessage.USER_VERIFICATION_CODE_ALREADY_EXISTS.getDesc();
//        }
//        int code = ((Double)(Math.random() * 10000)).intValue();
//        StringBuilder str = new StringBuilder(Integer.toString(code));
//        if (str.length() < 4) {
//            for (int i = str.length(); i < 4; i++) {
//                str.append("0");
//            }
//        }
//        redisTemplate.boundValueOps(RedisCode.USER_VERIFICATION_CODE.getKey() + email).set(str.toString(), 30, TimeUnit.MINUTES);
//        System.out.println(str.toString());
//        return ErrorMessage.USER_VERIFICATION_CODE_SEND_SUCCESS.getDesc();
//    }
//
//    @Override
//    public String registerUser(BlogUserVo blogUserVo) {
//        String username = blogUserVo.getUsername();
//        if (StringUtils.isBlank(username)) {
//            return "用户名不能为空";
//        }
//        if (StringUtils.isBlank(blogUserVo.getPassword())) {
//            return "密码不能为空";
//        }
//        BlogUser user = sysUserDAO.selectUserByUsername(username);
//        if (user != null){
//            return "用户名已存在";
//        }
//        if (Objects.equals(redisTemplate.hasKey(RedisCode.USER_VERIFICATION_CODE.getKey() + blogUserVo.getEmail()), Boolean.FALSE)) {
//            return "验证码不存在";
//        }
//        String code = (String) redisTemplate.boundValueOps(RedisCode.USER_VERIFICATION_CODE.getKey() + blogUserVo.getEmail()).get();
//        if (code != null && !code.equals(blogUserVo.getCode())) {
//            return "验证码错误";
//        }
//        blogUserVo.setPassword(passwordEncoder.encode(blogUserVo.getPassword()));
//        blogUserVo.setStatus(1);
//        blogUserVo.setCreateTime(new Date());
//        blogUserVo.setUpdateTime(blogUserVo.getCreateTime());
//        sysUserDAO.insert(blogUserVo);
//        sysUserDAO.insertUserRole(Collections.singletonList(2), blogUserVo.getId());
//        return "注册成功";
//    }
//
//    @Override
//    public BlogUser selectUserByUsername(String username) {
//        return sysUserDAO.selectUserByUsername(username);
//    }
//
//    @Override
//    public MyPage<SysUserVo> selectUserByPage(int page, int size) {
//        MyPage<SysUserVo> myPage = null;
//        PageHelper.startPage(page, size);
//        Page<BlogUser> userPage = (Page<BlogUser>) sysUserDAO.selectUserList();
//        Page<SysUserVo> userVoPage = new Page<>();
//        for (BlogUser user : userPage) {
//            SysUserVo sysUserVo = new SysUserVo();
//            BeanUtils.copyProperties(user, sysUserVo);
//            Set<SysRole> sysRoles = sysRoleService.selectRoleByUserId(user.getId());
//            sysUserVo.setSysRole(sysRoles);
//            sysUserVo.setRoleIds(sysRoles.parallelStream().map(SysRole::getId).collect(Collectors.toList()));
//            userVoPage.add(sysUserVo);
//        }
//        try {
//            myPage = MyPageUtils.pageUtil(userVoPage, userPage.getPageNum(), userPage.getPageSize(), (int) userPage.getTotal());
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//        return myPage;
//    }
//
//    @Override
//    public void updateUser(SysUserVo sysUserVo, Integer perFlag) {
//        sysUserDAO.updateUser(sysUserVo);
//        if (perFlag == 1) {
//            sysUserDAO.deleteUserRole(sysUserVo.getId());
//            sysUserDAO.insertUserRole(sysUserVo.getRoleIds(), sysUserVo.getId());
//        }
//    }
//}