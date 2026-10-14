package com.blog.core.utils;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

/**
 * @Author: lxk
 * @date 2022/7/20 11:28
 * @description:
 */

@Slf4j
public class IpUtil {

    private static final Logger logger = LoggerFactory.getLogger(IpUtil.class);

    private static final String IP_UNKNOWN = "unknown";
    private static final String IP_LOCAL = "127.0.0.1";
    private static final String IPV6_LOCAL = "0:0:0:0:0:0:0:1";
    private static final int IP_LEN = 15;

    /**
     * 获取用户真实IP地址，不直接使用request.getRemoteAddr();的原因是有可能用户使用了代理软件方式避免真实IP地址,
     * 可是，如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值，究竟哪个才是真正的用户端的真实IP呢？
     * 答案是取X-Forwarded-For中第一个非unknown的有效IP字符串。
     * 如：X-Forwarded-For：192.168.1.110, 192.168.1.120, 192.168.1.130,
     * 192.168.1.100
     * 用户真实IP为： 192.168.1.110
     *
     * @param request
     * @return
     */
    public static String getIpAddress(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String ipAddress = headers.getFirst("x-forwarded-for");
        if (ipAddress == null || ipAddress.isEmpty() || IP_UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = headers.getFirst("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || IP_UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = headers.getFirst("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || IP_UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = Optional.ofNullable(request.getRemoteAddress()).map(address -> address.getAddress().getHostAddress()).orElse("");
            if (IP_LOCAL.equals(ipAddress) || IPV6_LOCAL.equals(ipAddress)) {
                // 根据网卡取本机配置的IP
                try {
                    InetAddress inet = InetAddress.getLocalHost();
                    ipAddress = inet.getHostAddress();
                } catch (UnknownHostException e) {
                    log.error(e.getMessage());
                }
            }
        }

        // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ipAddress != null && ipAddress.length() > IP_LEN) {
            int index = ipAddress.indexOf(",");
            if (index > 0) {
                ipAddress = ipAddress.substring(0, index);
            }
        }
        return ipAddress;
    }

    /**
     * 接口获取ip地址
     *
     * @param request
     * @return
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("x-forwarded-for");

        if (ipAddress == null || ipAddress.isEmpty() || IP_UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }

        if (ipAddress == null || ipAddress.isEmpty() || IP_UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ipAddress == null || ipAddress.isEmpty() || IP_UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();

            if (IP_LOCAL.equals(ipAddress) || IPV6_LOCAL.equals(ipAddress)) {
                try {
                    InetAddress inet = InetAddress.getLocalHost();
                    ipAddress = inet.getHostAddress();
                } catch (UnknownHostException e) {
                    log.error(e.getMessage());
                }
            }
        }

        if (ipAddress != null && ipAddress.length() > IP_LEN) {
            int index = ipAddress.indexOf(",");
            if (index > 0) {
                ipAddress = ipAddress.substring(0, index);
            }
        }

        return ipAddress;
    }


    /**
     * 腾讯地图 IP 定位
     *
     * @param ip 待查询的 IP 地址
     * @return IP 定位信息
     */
    public static JSONObject getIpLocation(String ip) {
        JSONObject result = new JSONObject();
        try {
            // 腾讯地图 WebService API Key
            String key = "TOLBZ-5ZLLI-4VPGS-UIBPB-SPJCJ-BXFKM";
            String url = "https://apis.map.qq.com/ws/location/v1/ip" + "?ip=" + ip + "&key=" + key;

            // 请求腾讯地图 IP 定位接口
            String response = HttpUtil.get(url, 5000);
            JSONObject json = JSONObject.parseObject(response);

            // 腾讯地图 status = 0 表示请求成功
            if (json != null && json.getIntValue("status") == 0) {
                JSONObject data = json.getJSONObject("result");
                if (data == null) {
                    return result;
                }
                JSONObject location = data.getJSONObject("location");
                JSONObject adInfo = data.getJSONObject("ad_info");
                result.put("ip", ip);
                if (adInfo != null) {
                    result.put("country", adInfo.getString("nation"));
                    result.put("region", adInfo.getString("province"));
                    result.put("city", adInfo.getString("city"));

                    // 腾讯地图该接口不一定提供国家代码和运营商信息
                    result.put("countryCode", null);
                    result.put("isp", null);
                }

                if (location != null) {
                    result.put("lat", location.getBigDecimal("lat"));
                    result.put("lon", location.getBigDecimal("lng"));
                }

            } else {
                logger.warn("腾讯地图 IP 定位失败，ip={}，响应={}", ip, response);
            }
        } catch (Exception e) {
            logger.error("IP 定位失败，ip={}", ip, e);
        }

        return result;
    }
}
