package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    // 配置 Thymeleaf 模板解析器
    @Bean
    public ITemplateResolver templateResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCacheable(false);  // 开发期间可以禁用缓存
        templateResolver.setCharacterEncoding("UTF-8");  // 设置编码为 UTF-8
        return templateResolver;
    }

    // 配置 Thymeleaf 模板引擎
    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        return templateEngine;
    }

    // 配置 Thymeleaf 视图解析器
    @Bean
    public ThymeleafViewResolver viewResolver() {
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine());
        viewResolver.setCharacterEncoding("UTF-8");  // 设置视图解析器的编码为 UTF-8
        viewResolver.setOrder(1);
        return viewResolver;
    }

    // 配置静态资源的路径
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
//        registry.addResourceHandler("/jacoco/**").addResourceLocations("file:///D:/ruan2025/5yue7/studentinfo/studentinfo/jacoco/");
//        registry.addResourceHandler("/coverage/**").addResourceLocations("file:///D:/ruan2025/5yue7/studentinfo/studentinfo/coverage/");
//        registry.addResourceHandler("/apijacoco/**").addResourceLocations("file:///D:/ruan2025/5yue7/studentinfo/studentinfo/apijacoco/");
//        registry.addResourceHandler("/coverage/**").addResourceLocations("file:///D:/ruan2025/5yue7/studentinfo/studentinfo/coverageapi/");
        registry.addResourceHandler("/jacoco/**").addResourceLocations("file:///C:/Users/Wangfei/Desktop/new/studentinfo/jacoco/");
        registry.addResourceHandler("/coverage/**").addResourceLocations("file:///C:/Users/Wangfei/Desktop/new/studentinfo/coverage/");
        registry.addResourceHandler("/apijacoco/**").addResourceLocations("file:///C:/Users/Wangfei/Desktop/new/studentinfo/apijacoco/");
        registry.addResourceHandler("/coverageapi/**").addResourceLocations("file:///C:/Users/Wangfei/Desktop/new/studentinfo/coverageapi/");
    }


//    public void configureViewResolvers(ViewResolverRegistry registry) {
//        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
//        resolver.setPrefix("/WEB-INF/view/"); // JSP文件的前缀路径
//        resolver.setSuffix(".jsp"); // JSP文件的后缀
//        registry.viewResolver(resolver);
//    }
//    @Override
//    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer){
//        configurer.enable();
//    }
//    @Bean
//    public MultipartResolver multipartResolver() {
//        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
//        resolver.setMaxUploadSize(5242880); // 5MB
//        return resolver;
//    }
}
