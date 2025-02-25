package com.dmg.resserver.config;
 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
 
//解决跨域
@Configuration
public class CorsConfig implements WebMvcConfigurer {
 
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		//设置允许跨域的域名
		registry.addMapping("/**")
				//设置允许跨域的域名
				.allowedOriginPatterns("*")
				//是否允许cookie
				.allowCredentials(true)
				//设置允许请求的方式
				.allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
				//设置允许的请求头
				.allowedHeaders("*")
				//跨域允许时间
				.maxAge(3600)
				//设置允许跨域的域名
				.allowedOrigins("http://localhost:3001","http://localhost:3000","http://auth-server:8084",
						"http://res-server:8085","http://dianshang:8086","http://localhost:3002","http://localhost")
		;
	}
}