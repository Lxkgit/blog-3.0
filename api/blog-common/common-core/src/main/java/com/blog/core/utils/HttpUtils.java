package com.blog.core.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.blog.core.entity.auth.vo.Oauth2Vo;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;


import java.io.BufferedReader;
import java.util.*;

@Slf4j
public class HttpUtils {

    /**
     * 调用认证中心获取token专用
     *
     * @param url 认证服务器地址
     * @param params key-value格式
     * @return
     */
    public static Result doPost(String url, Map<String, String> params, Oauth2Vo vo) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();

            HttpPost httpPost = new HttpPost(url);
            //设置参数
            List<NameValuePair> nvs = new ArrayList<>();
            for (String name : params.keySet()) {
                String value = params.get(name);
                nvs.add(new BasicNameValuePair(name, value));
            }
            // 使用base64进行加密，将加密的字节信息转化为string类型，encoding--->token
            String str = vo.getClientId() + ":" + vo.getClientSecret();
            String encoding = new String(Base64.getEncoder().encode(str.getBytes()));
            //这里必须是Basic 认证客户端 否则会302重定向到登陆
            httpPost.setHeader("Authorization", "Basic " + encoding);
            httpPost.setEntity(new UrlEncodedFormEntity(nvs));

            String result = httpClient.execute(httpPost, classicHttpResponse -> {
                if (classicHttpResponse.getCode() != 200) {
                    return null;
                } else {
                    return EntityUtils.toString(classicHttpResponse.getEntity());
                }
            });
            if (result != null) {
                return ResultFactory.buildSuccessResult(JSONObject.parseObject(result));
            } else {
                return ResultFactory.buildFailResult("接口请求失败");
            }
        } catch (Exception e) {
            log.error("post请求异常:{}", e.getMessage(), e);
            return null;
        }

    }

}
