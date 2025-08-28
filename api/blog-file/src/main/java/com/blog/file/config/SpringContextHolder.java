package com.blog.file.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SpringContextHolder implements ApplicationContextAware {
    private static ApplicationContext applicationContext;
    private static Environment environment;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextHolder.applicationContext = applicationContext;
        SpringContextHolder.environment = applicationContext.getEnvironment();
    }

    public static ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext未初始化，请确保SpringContextHolder已在Spring容器中");
        }
        return applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

    public static boolean containsBean(String name) {
        return getApplicationContext().containsBean(name);
    }

    public static Environment getEnvironment() {
        return environment;
    }
}
