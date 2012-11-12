package com.sjl;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.*;

import org.eclipse.jetty.annotations.*;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.server.nio.*;
import org.eclipse.jetty.util.resource.*;
import org.eclipse.jetty.util.thread.*;
import org.eclipse.jetty.webapp.*;

public class WebServer
{
    public static interface WebContext
    {
        public File getWarPath();
        public String getContextPath();
    }
    
    private Server server;
    private WebServerConfig config;
    
    public WebServer(WebServerConfig aConfig)
    {
        config = aConfig;
    }
    
    public void start() throws Exception
    {
        server = new Server();

        server.setThreadPool(createThreadPool());
        server.addConnector(createConnector());
        server.setHandler(createHandlers());        
        server.setStopAtShutdown(true);
                
        server.start();
    }
    
    public void join() throws InterruptedException
    {
        server.join();
    }
    
    public void stop() throws Exception
    {        
        server.stop();
    }
    
    private ThreadPool createThreadPool()
    {
        QueuedThreadPool _threadPool = new QueuedThreadPool();
        _threadPool.setName(config.getServerName());
        _threadPool.setMinThreads(config.getMinThreads());
        _threadPool.setMaxThreads(config.getMaxThreads());
        return _threadPool;
    }
    
    private SelectChannelConnector createConnector()
    {
        SelectChannelConnector _connector = new SelectChannelConnector();
        _connector.setPort(config.getPort());
        _connector.setHost(config.getHostInterface());
        return _connector;
    }
    
    private HandlerCollection createHandlers()
    {       	    	
        WebAppContext _ctx = new WebAppContext();
        _ctx.setContextPath("/");
        _ctx.setBaseResource(Resource.newClassPathResource("META-INF/webapp"));        
        
		_ctx.setConfigurations (new Configuration []
		{
			// This is necessary because Jetty out-of-the-box does not scan
			// the classpath of your project in Eclipse, so it doesn't find
			// your WebAppInitializer.
			new AnnotationConfiguration() 
			{
				@Override
				public void configure(WebAppContext aContext) throws Exception
				{
					boolean _metadataComplete = aContext.getMetaData().isMetaDataComplete();
			        aContext.addDecorator(new AnnotationDecorator(aContext));   
			      
			        AnnotationParser _parser = null;
			        if (!_metadataComplete)
			        {
			            if (aContext.getServletContext().getEffectiveMajorVersion() >= 3 || aContext.isConfigurationDiscovered())
			            {
			                _parser = createAnnotationParser();
			                _parser.registerAnnotationHandler("javax.servlet.annotation.WebServlet", new WebServletAnnotationHandler(aContext));
			                _parser.registerAnnotationHandler("javax.servlet.annotation.WebFilter", new WebFilterAnnotationHandler(aContext));
			                _parser.registerAnnotationHandler("javax.servlet.annotation.WebListener", new WebListenerAnnotationHandler(aContext));
			            }
			        }
			       
			        List<ServletContainerInitializer> nonExcludedInitializers = getNonExcludedInitializers(aContext);
			        _parser = registerServletContainerInitializerAnnotationHandlers(aContext, _parser, nonExcludedInitializers);
			       
			        if (_parser != null)
			        {
			            parse(aContext, _parser);			            
			        }			       
				}
				
				private void parse(final WebAppContext aContext, AnnotationParser aParser) throws Exception
				{
					clearAnnotationList(aParser.getAnnotationHandlers());
					List<Resource> _resources = getResources(getClass().getClassLoader());
					for (Resource _resource : _resources)
					{
						if (_resource == null)
							return;
				
						ClassNameResolver _resolver = new ClassNameResolver()
		                {
		                    public boolean isExcluded (String name)
		                    {        	
		                        if (aContext.isSystemClass(name)) return true;                        	
		                        if (aContext.isServerClass(name)) return false;
		                        return false;
		                    }
		
		                    public boolean shouldOverride (String name)
		                    {
		                        //looking at webapp classpath, found already-parsed class of same name - did it come from system or duplicate in webapp?
		                        if (aContext.isParentLoaderPriority())
		                            return false;
		                        return true;
		                    }
		                };
						
						if (_resource.isDirectory())						
			                aParser.parse(_resource, _resolver);						
						else
							aParser.parse(_resource.getURI(), _resolver);
					}
		            
		            //TODO - where to set the annotations discovered from WEB-INF/classes?    
		            List<DiscoveredAnnotation> _annotations = new ArrayList<DiscoveredAnnotation>();
		            gatherAnnotations(_annotations, aParser.getAnnotationHandlers());	                
		            aContext.getMetaData().addDiscoveredAnnotations (_annotations);
				}
				
				private List<Resource> getResources(ClassLoader aLoader) throws IOException
				{
					if (aLoader instanceof URLClassLoader)
		            {
						List<Resource> _result = new ArrayList<Resource>();
		                URL[] _urls = ((URLClassLoader)aLoader).getURLs();		                
		                for (URL _url : _urls)
		                	_result.add(Resource.newResource(_url));
		
		                return _result;
		            }
					return Collections.emptyList();					
				}
			}
		});
        
        List<Handler> _handlers = new ArrayList<Handler>();
        
        _handlers.add(_ctx);
        
        HandlerList _contexts = new HandlerList();
        _contexts.setHandlers(_handlers.toArray(new Handler[0]));
        
        RequestLogHandler _log = new RequestLogHandler();
        _log.setRequestLog(createRequestLog());
        
        HandlerCollection _result = new HandlerCollection();
        _result.setHandlers(new Handler[] {_contexts, _log});
        
        return _result;
    }
    
    private RequestLog createRequestLog()
    {
        NCSARequestLog _log = new NCSARequestLog();
        
        File _logPath = new File(config.getAccessLogDirectory() + "yyyy_mm_dd.request.log");
        _logPath.getParentFile().mkdirs();
                
        _log.setFilename(_logPath.getPath());
        _log.setRetainDays(30);
        _log.setExtended(false);
        _log.setAppend(true);
        _log.setLogTimeZone("UTC");
        _log.setLogLatency(true);
        return _log;
    }    
}