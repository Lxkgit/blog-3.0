package com.blog.auth.config.filter;

import com.alibaba.fastjson2.JSONObject;
import com.blog.core.utils.JwtUtil;
import com.blog.core.utils.SecurityUtil;
import com.blog.redis.constant.AuthRedisConstant;
import com.blog.redis.service.RedisService;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * 认证过滤器 校验通过 就不需要再登陆
 */
@Slf4j
@Component
public class MyAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private RedisService redisService;

    /**
     * 所有请求的过滤器
     *
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain)
            throws ServletException, IOException {


        try {

            //从请求头获取认证id
            String rzId = request.getHeader("rzId");
            if (StringUtils.isNotEmpty(rzId)) {
                SecurityUtil.setRzId(rzId);
                //去redis中获取上下文
                String key = AuthRedisConstant.RZ_ID + ":" + rzId;
                // 根据缓存 获取认证信息
                Object o = redisService.getString(key);
                if (o == null) {
                    //如果缓存没有 那么放行
                    filterChain.doFilter(request, response);
                    return;
                }
                SecurityContext securityContext = (SecurityContext) o;
                //把上下文信息放入持有人手中 这样别的请求在进来 就有认证的权限了 就不需要再登陆了
                SecurityContextHolder.setContext(securityContext);
            }

            String token = request.getHeader("Authorization");
            if(StringUtils.isNotEmpty(token)) {
                JSONObject jwt = JwtUtil.decodeJwt(token.substring(7));
                SecurityUtil.setLoginUser(jwt);
            }

//                byte[] decoded =
//                // 使用KeyFactory生成RSAPrivateKey
//                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
//                PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
//
//
//                Cipher cipher = Cipher.getInstance("RSA");
//                cipher.init(Cipher.DECRYPT_MODE, privateKey);
//
////                byte[] jwtByte = Base64.getDecoder().decode(jwt);
//                byte[] decryptedBytes = cipher.doFinal(jwt.getBytes());
//
//                // 4. 将解密后的字节数组转为明文
//                String decryptedToken = new String(decryptedBytes, StandardCharsets.UTF_8);
//                System.out.println("解密后的 Token: " + decryptedToken);

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        //放行
        filterChain.doFilter(request, response);
    }


}
