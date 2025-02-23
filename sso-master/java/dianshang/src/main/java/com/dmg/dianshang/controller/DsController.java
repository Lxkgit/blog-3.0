package com.dmg.dianshang.controller;

import com.dmg.dianshang.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
public class DsController {

    @GetMapping("/cc")
    public Result cc(){
        return Result.success("我是电商项目ccc");
    }



}
