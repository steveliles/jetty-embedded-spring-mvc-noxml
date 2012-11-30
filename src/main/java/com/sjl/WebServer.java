package com.sjl;

import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.jetty.annotations.*;
import org.eclipse.jetty.annotations.AnnotationParser.DiscoverableAnnotationHandler;
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
				public void configure(WebAppContext context) throws Exception {
				       boolean metadataComplete = context.getMetaData().isMetaDataComplete();
				       context.addDecorator(new AnnotationDecorator(context));   
				      
				       
				       //Even if metadata is complete, we still need to scan for ServletContainerInitializers - if there are any
				       AnnotationParser parser = null;
				       if (!metadataComplete)
				       {
				           //If metadata isn't complete, if this is a servlet 3 webapp or isConfigDiscovered is true, we need to search for annotations
				           if (context.getServletContext().getEffectiveMajorVersion() >= 3 || context.isConfigurationDiscovered())
				           {
				               _discoverableAnnotationHandlers.add(new WebServletAnnotationHandler(context));
				               _discoverableAnnotationHandlers.add(new WebFilterAnnotationHandler(context));
				               _discoverableAnnotationHandlers.add(new WebListenerAnnotationHandler(context));
				           }
				       }
				       
				       //Regardless of metadata, if there are any ServletContainerInitializers with @HandlesTypes, then we need to scan all the
				       //classes so we can call their onStartup() methods correctly
				       createServletContainerInitializerAnnotationHandlers(context, getNonExcludedInitializers(context));
				       
				       if (!_discoverableAnnotationHandlers.isEmpty() || _classInheritanceHandler != null || !_containerInitializerAnnotationHandlers.isEmpty())
				       {           
				           parser = createAnnotationParser();
				           
				           parse(context, parser);
				           
				           for (DiscoverableAnnotationHandler h:_discoverableAnnotationHandlers)
				               context.getMetaData().addDiscoveredAnnotations(((AbstractDiscoverableAnnotationHandler)h).getAnnotationList());      
				       }

				}
				
				private void parse(final WebAppContext context, AnnotationParser parser) throws Exception
				{					
					List<Resource> _resources = getResources(getClass().getClassLoader());
					
					for (Resource _resource : _resources)
					{
						if (_resource == null)
							return;
		            
						parser.clearHandlers();
		                for (DiscoverableAnnotationHandler h:_discoverableAnnotationHandlers)
		                {
		                    if (h instanceof AbstractDiscoverableAnnotationHandler)
		                        ((AbstractDiscoverableAnnotationHandler)h).setResource(null); //
		                }
		                parser.registerHandlers(_discoverableAnnotationHandlers);
		                parser.registerHandler(_classInheritanceHandler);
		                parser.registerHandlers(_containerInitializerAnnotationHandlers);
		                
		                parser.parse(_resource, 
		                             new ClassNameResolver()
		                {
		                    public boolean isExcluded (String name)
		                    {
		                        if (context.isSystemClass(name)) return true;
		                        if (context.isServerClass(name)) return false;
		                        return false;
		                    }
		
		                    public boolean shouldOverride (String name)
		                    {
		                        //looking at webapp classpath, found already-parsed class of same name - did it come from system or duplicate in webapp?
		                        if (context.isParentLoaderPriority())
		                            return false;
		                        return true;
		                    }
		                });
		            }
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