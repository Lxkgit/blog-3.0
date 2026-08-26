package com.blog.file.controller;

import com.alibaba.fastjson2.JSONObject;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.file.service.CameraService;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @Description 视频流授权类
 * @Author lxk
 * @CreateTime 2026-08-26
 */

@RestController
@RequestMapping("/camera")
public class CameraController {

    @Resource
    private CameraService cameraService;


    /**
     * 创建视频临时 Token
     *
     * @param stream 视频流，例如 cam1
     * @return Token 信息
     */
    @PostMapping("/token")
    public Result createToken(@RequestParam String stream) {
        return ResultFactory.buildSuccessResult(cameraService.createToken(stream));
    }


    /**
     * 验证视频 Token
     * 此接口专门提供给 Nginx auth_request 使用
     * Token 有效：HTTP 200
     * Token 无效：HTTP 401
     *
     * @param token Token
     */
    @GetMapping("/token/validate")
    public ResponseEntity<Void> validateToken(@RequestParam String token) {
        JSONObject tokenInfo = cameraService.validateToken(token);
        if (tokenInfo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok().build();
    }


    /**
     * 刷新视频 Token
     *
     * @param token Token
     * @return 刷新结果
     */
    @PostMapping("/token/refresh")
    public Result refreshToken(@RequestParam String token) {
        boolean success = cameraService.refreshToken(token);
        if (!success) {
            return ResultFactory.buildFailResult("Token无效或已过期");
        }
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("expiresIn", cameraService.getExpire(token));
        return ResultFactory.buildSuccessResult(result);
    }


    /**
     * 主动撤销视频 Token
     *
     * @param token Token
     * @return 操作结果
     */
    @PostMapping("/token/revoke")
    public Result revokeToken(@RequestParam String token) {
        cameraService.revokeToken(token);
        return ResultFactory.buildSuccessResult();
    }
}