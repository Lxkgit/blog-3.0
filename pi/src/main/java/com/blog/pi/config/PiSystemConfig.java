package com.blog.pi.config;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.pi.dao.RegisterSettingDAO;
import com.blog.pi.domain.entity.RegisterSetting;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: 加载配置
 * @Author: lxk
 * @date 2024/1/29 15:56
 */

@Slf4j
@Component
public class PiSystemConfig {

    /**
     * 服务启动类型
     */
    @Value("${spring.profiles.active}")
    private String type;

    public static Map<String, JSONObject> registerConfigMap = new HashMap<>();

    @Resource
    private RegisterSettingDAO registerSettingDAO;

    /**
     * 加载注册配置信息
     */
    private void initRegisterConfig() {
        QueryWrapper<RegisterSetting> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("setting_type", type);
        List<RegisterSetting> registerSettingList = registerSettingDAO.selectList(queryWrapper);
        for (RegisterSetting r : registerSettingList) {
            registerConfigMap.put(r.getSettingName(), JSONObject.parseObject(r.getSetting()));
        }
    }

    public Object getRegisterConfig(String settingName, String settingField) {
        if (CollectionUtils.isEmpty(registerConfigMap)) {
            initRegisterConfig();
        }
        if (!registerConfigMap.containsKey(settingName)) {
            log.error("配置信息:{} 不存在", settingName);
        }
        JSONObject jsonObject = registerConfigMap.get(settingName);
        return jsonObject.get(settingField);
    }


}
