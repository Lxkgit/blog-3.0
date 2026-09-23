package com.blog.gateway.service.impl;

import com.blog.gateway.dao.BlacklistIpDAO;
import com.blog.gateway.service.BlacklistIpService;
//import com.blog.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: lxk
 * @date 2022/7/20 17:29
 * @description: 黑名单接口服务
 */

@Service
public class BlacklistIpServiceImpl implements BlacklistIpService {

    @Resource
    private BlacklistIpDAO blacklistIpDAO;

//    @Resource
//    private RedisService redisService;

    @Override
    public List<String> selectBlacklistIpList() {

//        redisService.setString("test", "aaa", 6000);

        return blacklistIpDAO.selectIpList();
    }
}
