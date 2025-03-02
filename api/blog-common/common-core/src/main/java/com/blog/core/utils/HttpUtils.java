package com.blog.core.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.blog.core.entity.auth.vo.Oauth2Vo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
public class HttpUtils {

    /**
     * post请求
     * @param url
     * @param params key-value格式
     * @return
     */
    public static JSONObject doPost(String url, Map params, Oauth2Vo vo){
        JSONObject json = new JSONObject();
        BufferedReader in;
        try {
            HttpClient client = HttpClients.createDefault();
            HttpPost request = new HttpPost(url);

            //设置参数
            List<NameValuePair> nvps = new ArrayList<>();
            for (Iterator iter = params.keySet().iterator(); iter.hasNext();) {
                String name = (String) iter.next();
                String value = String.valueOf(params.get(name));
                nvps.add(new BasicNameValuePair(name, value));
            }
            // 使用base64进行加密，将加密的字节信息转化为string类型，encoding--->token
            String str=vo.getClientId()+":"+vo.getClientSecret();
            String encoding = DatatypeConverter.printBase64Binary(str.getBytes(StandardCharsets.UTF_8));

            //这里必须是Basic 认证客户端 否则会302重定向到登陆
            request.setHeader("Authorization", "Basic " + encoding);
            request.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));

//            request.setHeader("Content-Type", "application/form-data");

            HttpResponse response = client.execute(request);
            log.info(response.toString());
            int statusCode = response.getStatusLine().getStatusCode();
            if(statusCode == 200){   //请求成功
                in = new BufferedReader(new InputStreamReader(response.getEntity()
                        .getContent(), StandardCharsets.UTF_8));
                StringBuffer sb = new StringBuffer();
                String line = "";
                String NL = System.getProperty("line.separator");
                while ((line = in.readLine()) != null) {
                    sb.append(line + NL);
                }
                in.close();
                json = JSON.parseObject(sb.toString());
                json.put("statusCode",statusCode);
                return json;
            } else{
                json.put("statusCode",statusCode);
                json.put("msg","参数错误,请重新获取授权码,授权码只能使用一次,请检查客户端和回调地址是否输入正确");
                log.info("post请求出错了,错误码:{}",statusCode);
            }
        } catch(Exception e){
            log.error("post请求异常:{}",e.getMessage(), e);
            return null;
        }
        return json;
    }

}
