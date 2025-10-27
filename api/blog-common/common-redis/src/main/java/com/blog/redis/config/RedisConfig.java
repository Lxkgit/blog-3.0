package com.blog.redis.config;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.*;
import org.springframework.security.jackson2.CoreJackson2Module;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * redis配置
 *
 * @author kangxu
 */
@Configuration
@EnableCaching
@AutoConfigureBefore(RedisAutoConfiguration.class)
public class RedisConfig {

    @Bean
    @SuppressWarnings("all")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // JSON 序列化配置
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();

        // 可见性与类型信息
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        //下面这2行必须设置 否则无法转换对象
        om.activateDefaultTyping(om.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        // 添加Security提供的Jackson Mixin
        om.registerModule(new CoreJackson2Module());

        // 设置时区为北京时间
        om.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        om.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        jackson2JsonRedisSerializer.setObjectMapper(om);
        // String 序列化
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // value序列化方式采用jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash的value序列化方式采用jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }
}