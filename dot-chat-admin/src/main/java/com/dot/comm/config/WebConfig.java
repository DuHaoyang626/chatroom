package com.dot.comm.config;

import com.dot.comm.filter.LogMDCFilter;
import com.dot.comm.interceptor.AccessLimitInterceptor;
import com.dot.comm.interceptor.AdminAuthInterceptor;
import com.dot.comm.interceptor.AdminTokenInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * token验证拦截器
 */
@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 这里使用一个Bean为的是可以在拦截器中自由注入，也可以在拦截器中使用SpringUtil.getBean 获取
    // 但是觉得这样更优雅

    @Bean
    public HandlerInterceptor adminTokenInterceptor() {
        return new AdminTokenInterceptor();
    }

    @Bean
    public HandlerInterceptor adminAuthInterceptor() {
        return new AdminAuthInterceptor();
    }

    @Bean
    public HandlerInterceptor accessLimitInterceptor() {
        log.debug("🔧 [管理后台] 创建访问限制拦截器Bean");
        return new AccessLimitInterceptor();
    }

    /**
     * 不需要拦截的静态资源
     */
    final String[] excludeStaticPathPatterns = {"/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**"};

    /**
     * 不需要拦截的接口
     */
    final String[] excludeApiPathPatterns = {"/api/sys/auth/admin/login","/api/sys/auth/admin/logout"};
    /**
     * 不需要校验菜单权限的接口
     */
    final String[] excludeApiPathPatterns2 = {"/api/sys/auth/role/menu/list","/api/sys/auth/admin/refreshToken"};

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("🔧 [管理后台] 配置拦截器开始");
        
        // 限流限制拦截器
        log.debug("📝 [管理后台] 添加访问限制拦截器，拦截路径: /**");
        registry.addInterceptor(accessLimitInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(excludeStaticPathPatterns);

        // 用户登录token 拦截器
        registry.addInterceptor(adminTokenInterceptor())
                .addPathPatterns("/api/sys/**", "/api/chat/**")
                .excludePathPatterns(excludeStaticPathPatterns)
                .excludePathPatterns(excludeApiPathPatterns);

        // 用户菜单权限拦截器
        registry.addInterceptor(adminAuthInterceptor())
                .addPathPatterns("/api/sys/**", "/api/chat/**")
                .excludePathPatterns(excludeStaticPathPatterns)
                .excludePathPatterns(excludeApiPathPatterns2)
                .excludePathPatterns(excludeApiPathPatterns);

        log.info("✅ [管理后台] 拦截器配置完成");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("🔧 [管理后台] 配置静态资源处理器开始");
        
        // 只处理静态资源，不拦截API请求
        log.debug("📁 [管理后台] 添加静态资源映射: /static/** -> classpath:/static/");
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        
        log.debug("📁 [管理后台] 添加favicon映射: /favicon.ico -> classpath:/static/");
        registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/static/");
        
        log.debug("📁 [管理后台] 添加图标映射: /ico/** -> classpath:/static/ico/");
        registry.addResourceHandler("/ico/**").addResourceLocations("classpath:/static/ico/");
        
        log.debug("📁 [管理后台] 添加API文档映射: doc.html -> classpath:/META-INF/resources/");
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        
        log.debug("📁 [管理后台] 添加webjars映射: /webjars/** -> classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        
        log.info("✅ [管理后台] 静态资源处理器配置完成");
    }

    @Bean
    public FilterRegistrationBean<LogMDCFilter> logFilterRegistration() {
        log.debug("🔧 [管理后台] 创建日志MDC过滤器Bean");
        FilterRegistrationBean<LogMDCFilter> registration = new FilterRegistrationBean<>();
        // 注入过滤器
        registration.setFilter(new LogMDCFilter());
        // 拦截规则
        registration.addUrlPatterns("/*");
        // 过滤器名称
        registration.setName("logMDCFilter");
        // 过滤器顺序
        registration.setOrder(0);
        log.debug("📝 [管理后台] 日志MDC过滤器配置: 拦截路径=/*，顺序=0");
        return registration;
    }
}
