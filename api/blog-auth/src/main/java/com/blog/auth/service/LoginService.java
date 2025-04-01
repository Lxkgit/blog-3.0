package com.blog.auth.service;



import com.blog.core.domain.auth.vo.LoginVo;
import com.blog.core.result.Result;
import jakarta.servlet.http.HttpServletRequest;

public interface LoginService {

    Result login(LoginVo vo);

    void tuiChu(String rzId);
}
