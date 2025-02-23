package com.dmg.kucun.controller;

import com.dmg.kucun.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
public class KcController {


    @GetMapping("/test")
    public Result test(){
        //获取资源服务器的数据
        log.info("进入库存项目test方法:");
        return Result.success("我是库存test");
    }


}
