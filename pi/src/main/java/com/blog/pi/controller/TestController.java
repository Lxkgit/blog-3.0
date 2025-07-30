package com.blog.pi.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.blog.pi.mqtt.MqttMessageListener;
import com.blog.pi.utils.excel.MyExcelUtil;
import com.blog.pi.utils.excel.demo.PlayerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description 测试类
 * @Author lxk
 * @CreateTime 2025-07-10
 */

@RestController
@RequestMapping("/test")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @PostMapping(value = "/import")
    public void importBatch(@RequestParam("file") MultipartFile file) throws IOException {

        List<PlayerRecord> list = new ArrayList<>();
        // 读取数据并转换为Java对象
        EasyExcel.read(file.getInputStream(), PlayerRecord.class, new PageReadListener<PlayerRecord>(list::addAll))
                .headRowNumber(2)
                .sheet()
                .doRead();
        System.out.println(list);


        logger.info(list.toString());

    }
}
