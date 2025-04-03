package com.blog.file.netty.schedule;

import com.alibaba.fastjson2.JSONObject;
import com.blog.file.netty.domain.common.NettyConstant;
import com.blog.file.netty.domain.dto.NettyPacket;
import com.blog.file.netty.domain.dto.file.NettySyncBlogFileDto;
import com.blog.file.netty.domain.enums.NettyPacketType;
import com.blog.file.netty.domain.enums.NettyTopicEnum;
import com.blog.file.netty.service.NettyServer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @description: 博客数据定时同步
 * @Author: lxk
 * @date 2024/1/11 10:26
 */

@Slf4j
@Configuration     //证明这个类是一个配置文件
@EnableScheduling  //启用定时器
public class SyncBlogFileSchedule {

    @Resource
    private NettyServer nettyServer;


    /**
     * 定时同步博客数据
     *
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void initFile() {
        log.info("文件同步");

        NettySyncBlogFileDto nettySyncBlogFile = new NettySyncBlogFileDto();
        nettySyncBlogFile.setSyncType(2);
        nettySyncBlogFile.setFileCode("blog.zip");
        nettySyncBlogFile.setFileName("blog.zip");
        nettySyncBlogFile.setFilePath("/sync");

        NettyPacket<NettySyncBlogFileDto> nettyResponse = NettyPacket.buildRequest(nettySyncBlogFile);
        nettyResponse.setNettyPacketType(NettyPacketType.REQUEST.getValue());
        nettyResponse.setTopic(NettyTopicEnum.BLOG_FILE_SYNC.getTopic());

        nettyServer.channelWriteByRegisterId(NettyConstant.NETTY_CLIENT1, JSONObject.toJSONString(nettyResponse));

    }


}
