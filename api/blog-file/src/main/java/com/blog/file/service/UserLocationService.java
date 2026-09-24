package com.blog.file.service;

import com.blog.core.domain.file.ip.entity.IpLocation;

import java.util.List;

/**
 * @author lxk
 * @description 用户ip定位服务
 * @date 2026/09/24
 */

public interface UserLocationService {

    List<IpLocation> selectIpLocationList();
}
