package com.dot.comm.config;

import com.dot.comm.filter.FrontTokenInterceptor;
import com.dot.comm.filter.LogMDCFilter;
import com.dot.comm.interceptor.AccessLimitInterceptor;
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
    public HandlerInterceptor frontTokenInterceptor() {
        log.debug("🔧 创建前端Token拦截器Bean");
        return new FrontTokenInterceptor();
    }

    @Bean
    public HandlerInterceptor accessLimitInterceptor() {
        log.debug("🔧 创建访问限制拦截器Bean");
        return new AccessLimitInterceptor();
    }

    final String[] excludePathPatterns = {"/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**"};

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("🔧 配置拦截器开始");
        
        // 添加token拦截器
        // addPathPatterns添加需要拦截的命名空间；
        // excludePathPatterns添加排除拦截命名空间
        // 限流限制拦截器
        log.debug("📝 添加访问限制拦截器，拦截路径: /**");
        registry.addInterceptor(accessLimitInterceptor()).addPathPatterns("/**")
                .excludePathPatterns(excludePathPatterns);

        // 前端用户登录token
        log.debug("📝 添加前端Token拦截器，拦截路径: /api/sys/**, /api/msg/**");
        log.debug("📝 排除路径: /api/sys/user/login, /api/sys/user/registerAndLogin");
        registry.addInterceptor(frontTokenInterceptor()).addPathPatterns("/api/sys/**","/api/msg/**")
                .excludePathPatterns(
                        "/api/sys/user/login",
                        "/api/sys/user/registerAndLogin"
                )
                .excludePathPatterns(excludePathPatterns);

        log.info("✅ 拦截器配置完成");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("🔧 配置静态资源处理器开始");
        
        // 添加外部静态资源映射 - 指向dot-chat-web项目
        String webappPath = "file:" + System.getProperty("user.dir") + "/../dot-chat-web/src/main/webapp/";
        log.debug("📁 添加外部静态资源映射: /*.html -> " + webappPath);
        registry.addResourceHandler("/*.html").addResourceLocations(webappPath);
        
        log.debug("📁 添加JS资源映射: /js/** -> " + webappPath);
        registry.addResourceHandler("/js/**").addResourceLocations(webappPath);
        
        log.debug("📁 添加CSS资源映射: /css/** -> " + webappPath);
        registry.addResourceHandler("/css/**").addResourceLocations(webappPath);
        
        log.debug("📁 添加图片资源映射: /images/** -> " + webappPath);
        registry.addResourceHandler("/images/**").addResourceLocations(webappPath);
        
        log.debug("📁 添加移动端资源映射: /mobile/** -> " + webappPath);
        registry.addResourceHandler("/mobile/**").addResourceLocations(webappPath);
        
        log.debug("📁 添加音频资源映射: /mp3/** -> " + webappPath);
        registry.addResourceHandler("/mp3/**").addResourceLocations(webappPath);
        
        // 添加内部静态资源映射
        log.debug("📁 添加内部静态资源映射: /static/** -> classpath:/static/");
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        
        log.debug("📁 添加favicon映射: /favicon.ico -> classpath:/static/");
        registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/static/");
        
        log.debug("📁 添加图标映射: /ico/** -> classpath:/static/ico/");
        registry.addResourceHandler("/ico/**").addResourceLocations("classpath:/static/ico/");
        
        log.debug("📁 添加API文档映射: doc.html -> classpath:/META-INF/resources/");
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        
        log.debug("📁 添加webjars映射: /webjars/** -> classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        
        log.info("✅ 静态资源处理器配置完成");
    }

    @Bean
    public FilterRegistrationBean<LogMDCFilter> logFilterRegistration() {
        log.debug("🔧 创建日志MDC过滤器Bean");
        FilterRegistrationBean<LogMDCFilter> registration = new FilterRegistrationBean<>();
        // 注入过滤器
        registration.setFilter(new LogMDCFilter());
        // 拦截规则
        registration.addUrlPatterns("/*");
        // 过滤器名称
        registration.setName("logMDCFilter");
        // 过滤器顺序
        registration.setOrder(0);
        log.debug("📝 日志MDC过滤器配置: 拦截路径=/*，顺序=0");
        return registration;
    }
}
