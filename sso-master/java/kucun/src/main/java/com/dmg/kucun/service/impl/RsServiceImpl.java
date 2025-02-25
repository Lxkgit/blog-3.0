package com.dmg.kucun.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dmg.kucun.service.RsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * 访问资源服务器 前后分离使用
 */
@Slf4j
@Service
public class RsServiceImpl implements RsService {

    @Autowired
    private RestTemplate restTemplate;


    /**
     * 获取资源服务器的认证授权信息
     *
     * @param token 令牌
     * @return
     * @throws Exception
     */
    public Authentication getAuthentication(String token) {
        HttpHeaders headers = new HttpHeaders();
        //把token 放入请求头中 ,token类型为Bearer
        //在源码里面this.set("Authorization", "Bearer " + token) 自动拼接Bearer空格
        headers.setBearerAuth(token);
        HttpEntity<String> request = new HttpEntity<>("", headers);

        try {
            //拿着令牌去获取资源服务器的接口
            ResponseEntity<String> responseEntity = restTemplate
                    .exchange("http://res-server:8085/getAuthentication",
                            HttpMethod.POST, request, String.class);
            log.info("============", responseEntity);
            String body = responseEntity.getBody();
            //解析成认证对象
            JSONObject jsonObject = JSONObject.parseObject(body);
            //获取登陆人账号
            String username = jsonObject.getString("name");
            //权限集合
            List<GrantedAuthority> authorities = new ArrayList<>();
            //获取权限
            JSONArray jsonArray = jsonObject.getJSONArray("authorities");
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String str = obj.getString("authority");
                //把权限放入对象
                SimpleGrantedAuthority sga = new SimpleGrantedAuthority(str);
                //把对象放入权限集合
                authorities.add(sga);
            }
            // 构造新的 Authentication 对象
            Authentication authentication = new UsernamePasswordAuthenticationToken(username
                    , null, authorities);
            return authentication;
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
        }
        return null;
    }

}
