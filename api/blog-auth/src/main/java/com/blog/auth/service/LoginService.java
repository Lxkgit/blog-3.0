package com.blog.auth.service;



import com.alibaba.fastjson2.JSONObject;
import com.blog.core.domain.auth.vo.LoginVo;
import com.blog.core.domain.auth.vo.Oauth2Vo;
import com.blog.core.exception.ServiceException;
import com.blog.core.result.Result;

public interface LoginService {

    String register(LoginVo vo);

    JSONObject getToken(Oauth2Vo vo) throws ServiceException;

}
