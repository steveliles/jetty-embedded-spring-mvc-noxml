package com.sjl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

@EnableWebMvc
@Configuration
@ComponentScan(basePackages={"com.sjl"})
public class WebModule extends WebMvcConfigurerAdapter
{
	@Override
	public void addViewControllers(ViewControllerRegistry aRegistry)
	{
		aRegistry.addViewController("/").setViewName("index");
	}
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry aRegistry)
	{
		aRegistry.addResourceHandler("/s/*").addResourceLocations("classpath:/META-INF/webapp/WEB-INF/view/scripts/*");
		aRegistry.addResourceHandler("/c/*").addResourceLocations("classpath:/META-INF/webapp/WEB-INF/view/css/*");
		aRegistry.addResourceHandler("/i/*").addResourceLocations("classpath:/META-INF/webapp/WEB-INF/view/images/*");
		aRegistry.addResourceHandler("/WEB-INF/view/*").addResourceLocations("classpath:/META-INF/webapp/WEB-INF/view/*");
		aRegistry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/META-INF/webapp/WEB-INF/view/images/favicon.ico");
	}
	
	@Bean
	public ViewResolver viewResolver() 
	{
		UrlBasedViewResolver viewResolver = new UrlBasedViewResolver();
		viewResolver.setViewClass(JstlView.class);
		viewResolver.setPrefix("WEB-INF/view/");
		viewResolver.setSuffix(".jsp");
		return viewResolver;
	}
}
