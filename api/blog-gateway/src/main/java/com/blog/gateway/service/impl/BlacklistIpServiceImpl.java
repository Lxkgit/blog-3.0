package com.blog.gateway.service.impl;

import com.blog.gateway.mapper.BlacklistIpMapper;
import com.blog.gateway.service.BlacklistIpService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: lxk
 * @date 2022/7/20 17:29
 * @description: 黑名单接口服务
 */

@Service
public class BlacklistIpServiceImpl implements BlacklistIpService {

    @Resource
    private BlacklistIpMapper blacklistIpDAO;



    @Override
    public List<String> selectBlacklistIpList() {
        return blacklistIpDAO.selectIpList();
    }
}
