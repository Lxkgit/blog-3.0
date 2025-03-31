package com.blog.file.socket;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;


/**
 * @description: websocket 编码
 * @Author: lxk
 * @date 2023/6/8 15:53
 */

public class ServerEncoder implements Encoder.Text<AjaxResult> {
    @Override
    public String encode(AjaxResult ajaxResult) throws EncodeException {
        JSON parse = JSONUtil.parseObj(ajaxResult);
        return parse.toString();
    }

    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }
}

