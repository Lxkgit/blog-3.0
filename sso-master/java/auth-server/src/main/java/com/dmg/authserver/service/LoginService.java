package com.dmg.authserver.service;


import com.dmg.authserver.utils.Result;
import com.dmg.authserver.vo.LoginVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public interface LoginService {

    public Result login(LoginVo vo, HttpServletRequest request);

    public void tuiChu(String rzId);
}
