package com.sjl.config;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class WebAppInitializer implements WebApplicationInitializer
{
	private static final String DISPATCHER_SERVLET_NAME = "dispatcher";
	
	@Override
	public void onStartup(ServletContext aServletContext) throws ServletException
	{		
		registerListener(aServletContext);
		registerDispatcherServlet(aServletContext);
	}
	
	private void registerListener(ServletContext aContext)
	{
		AnnotationConfigWebApplicationContext _root = createContext(ApplicationModule.class);
		aContext.addListener(new ContextLoaderListener(_root));
	}
	
	private void registerDispatcherServlet(ServletContext aContext)
	{
		AnnotationConfigWebApplicationContext _ctx = createContext(WebModule.class);
		ServletRegistration.Dynamic _dispatcher = 
			aContext.addServlet(DISPATCHER_SERVLET_NAME, new DispatcherServlet(_ctx));
		_dispatcher.setLoadOnStartup(1);
		_dispatcher.addMapping("/");
	}

	private AnnotationConfigWebApplicationContext createContext(final Class<?>... aModules)
	{
		AnnotationConfigWebApplicationContext _ctx = new AnnotationConfigWebApplicationContext();
		_ctx.register(aModules);
		return _ctx;
	}
}
