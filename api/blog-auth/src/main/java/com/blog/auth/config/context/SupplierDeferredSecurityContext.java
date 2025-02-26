package com.blog.auth.config.context;

import org.springframework.security.core.context.DeferredSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolderStrategy;

import java.util.function.Supplier;

/**
 * 提供者延迟上下文
 */
public class SupplierDeferredSecurityContext implements DeferredSecurityContext {

    //提供者上下文
    private  Supplier<SecurityContext>  supplier;
    //安全上下文持有者策略
    private SecurityContextHolderStrategy securityContextHolderStrategy;

    //安全上下文
    private SecurityContext securityContext;

    private boolean flag;

    public SupplierDeferredSecurityContext(Supplier<SecurityContext> supplier,
                                           SecurityContextHolderStrategy securityContextHolderStrategy){
        this.securityContextHolderStrategy=securityContextHolderStrategy;
        this.supplier=supplier;
    }

    @Override
    public boolean isGenerated() {
        //先初始化
        init();
        //在返回是否创建上下文
        return flag;
    }

    @Override
    public SecurityContext get() {
        //先初始化
        init();
        //在获取上下文
        return this.securityContext;
    }

    private void init(){
        //如果上下文有值 不需要初始化
        if(securityContext!=null){
            return;
        }
        this.securityContext=supplier.get();
        if(this.securityContext==null){
            //如果提供者 也没有上下文
            this.flag=true;
        }
        if(this.flag){
            //如果没有创建上下文 那么从策略中创建上下文
            this.securityContext=this.securityContextHolderStrategy.createEmptyContext();
        }

    }
}
