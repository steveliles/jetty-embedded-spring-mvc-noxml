package com.sjl.config;

import org.apache.jasper.servlet.JspServlet;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

public class WebAppInitializer implements WebApplicationInitializer {
    private static final String JSP_SERVLET_NAME = "jsp";
    private static final String DISPATCHER_SERVLET_NAME = "dispatcher";

    @Override
    public void onStartup(ServletContext aServletContext) throws ServletException {
        registerListener(aServletContext);
        registerDispatcherServlet(aServletContext);
        registerJspServlet(aServletContext);
    }

    private void registerListener(ServletContext aContext) {
        AnnotationConfigWebApplicationContext _root = createContext(ApplicationModule.class);
        aContext.addListener(new ContextLoaderListener(_root));
    }

    private void registerDispatcherServlet(ServletContext aContext) {
        AnnotationConfigWebApplicationContext _ctx = createContext(WebModule.class);
        ServletRegistration.Dynamic _dispatcher =
                aContext.addServlet(DISPATCHER_SERVLET_NAME, new DispatcherServlet(_ctx));
        _dispatcher.setLoadOnStartup(1);
        _dispatcher.addMapping("/");
    }

    private void registerJspServlet(ServletContext aContext) {
        ServletRegistration.Dynamic _dispatcher =
                aContext.addServlet(JSP_SERVLET_NAME, new JspServlet());
        _dispatcher.setLoadOnStartup(1);
        _dispatcher.addMapping("*.jsp");
    }

    private AnnotationConfigWebApplicationContext createContext(final Class<?>... aModules) {
        AnnotationConfigWebApplicationContext _ctx = new AnnotationConfigWebApplicationContext();
        _ctx.register(aModules);
        return _ctx;
    }
}
