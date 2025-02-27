package com.blog.auth.service;



import com.blog.core.entity.auth.vo.LoginVo;
import com.blog.core.result.Result;
import jakarta.servlet.http.HttpServletRequest;

public interface LoginService {

    public Result login(LoginVo vo, HttpServletRequest request);

    public void tuiChu(String rzId);
}
