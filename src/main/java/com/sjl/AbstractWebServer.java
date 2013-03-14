package com.sjl;

import com.google.common.base.Optional;
import com.sjl.config.ServerConfig;
import com.sjl.util.Duration;
import org.eclipse.jetty.annotations.*;
import org.eclipse.jetty.annotations.AnnotationParser.DiscoverableAnnotationHandler;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractWebServer {

    private final Server server = new Server();
    private final ServerConfig config = new ServerConfig();

    public abstract void initialize(ServerConfig config);

    public final void run(String[] args) throws Exception {

        //todo parse args

        initialize(config);

        server.addConnector(createConnector());
        server.addConnector(createAdminConnector());
        server.setSendDateHeader(true);
        server.setSendServerVersion(false);

        server.setThreadPool(createThreadPool());
        server.setHandler(createHandlers());

        server.setStopAtShutdown(true);
        server.setGracefulShutdown((int) config.getShutdownGracePeriod().toMilliseconds());

        server.start();
        server.join();
    }

    public void shutdown() throws Exception {
        server.stop();
    }

    private ThreadPool createThreadPool() {
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setName(config.getName());
        threadPool.setMinThreads(config.getMinThreads());
        threadPool.setMaxThreads(config.getMaxThreads());
        return threadPool;
    }


    private Connector createAdminConnector() {
        final SocketConnector connector = new SocketConnector();
        connector.setHost(config.getIp());
        connector.setPort(config.getAdminPort());
        connector.setName("admin-monitor");
        connector.setThreadPool(new QueuedThreadPool(8));
        return connector;
    }

    private AbstractConnector createConnector() {
        AbstractConnector connector = new SelectChannelConnector();
        connector.setPort(config.getPort());
        connector.setHost(config.getIp());
        connector.setAcceptors(config.getAcceptorThreads());
        connector.setForwarded(config.isUseForwardedHeaders());

        connector.setMaxIdleTime((int) config.getMaxIdleTime().toMilliseconds());
        connector.setLowResourcesMaxIdleTime((int) config.getLowResourcesMaxIdleTime().toMilliseconds());
        connector.setAcceptorPriorityOffset(config.getAcceptorThreadPriorityOffset());
        connector.setAcceptQueueSize(config.getAcceptQueueSize());
        connector.setMaxBuffers(config.getMaxBufferCount());
        connector.setRequestBufferSize((int) config.getRequestBufferSize().toBytes());
        connector.setRequestHeaderSize((int) config.getRequestHeaderBufferSize().toBytes());
        connector.setResponseBufferSize((int) config.getResponseBufferSize().toBytes());
        connector.setResponseHeaderSize((int) config.getResponseHeaderBufferSize().toBytes());
        connector.setReuseAddress(config.isReuseAddress());
        final Optional<Duration> lingerTime = config.getSoLingerTime();

        if (lingerTime.isPresent()) {
            connector.setSoLingerTime((int) lingerTime.get().toMilliseconds());
        }

        connector.setName("main");

        return connector;
    }


    private HandlerCollection createHandlers() {
        WebAppContext ctx = new WebAppContext();
        ctx.setContextPath("/");
        ctx.setBaseResource(Resource.newClassPathResource("META-INF/webapp"));

        ctx.setConfigurations(new Configuration[]{
                // This is necessary because Jetty out-of-the-box does not scan
                // the classpath of your project in Eclipse, so it doesn't find
                // your WebAppInitializer.
                new AnnotationConfiguration() {
                    @Override
                    public void configure(WebAppContext context) throws Exception {
                        boolean metadataComplete = context.getMetaData().isMetaDataComplete();
                        context.addDecorator(new AnnotationDecorator(context));


                        //Even if metadata is complete, we still need to scan for ServletContainerInitializers - if there are any
                        AnnotationParser parser = null;
                        if (!metadataComplete) {
                            //If metadata isn't complete, if this is a servlet 3 webapp or isConfigDiscovered is true, we need to search for annotations
                            if (context.getServletContext().getEffectiveMajorVersion() >= 3 || context.isConfigurationDiscovered()) {
                                _discoverableAnnotationHandlers.add(new WebServletAnnotationHandler(context));
                                _discoverableAnnotationHandlers.add(new WebFilterAnnotationHandler(context));
                                _discoverableAnnotationHandlers.add(new WebListenerAnnotationHandler(context));
                            }
                        }

                        //Regardless of metadata, if there are any ServletContainerInitializers with @HandlesTypes, then we need to scan all the
                        //classes so we can call their onStartup() methods correctly
                        createServletContainerInitializerAnnotationHandlers(context, getNonExcludedInitializers(context));

                        if (!_discoverableAnnotationHandlers.isEmpty() || _classInheritanceHandler != null || !_containerInitializerAnnotationHandlers.isEmpty()) {
                            parser = createAnnotationParser();

                            parse(context, parser);

                            for (DiscoverableAnnotationHandler h : _discoverableAnnotationHandlers)
                                context.getMetaData().addDiscoveredAnnotations(((AbstractDiscoverableAnnotationHandler) h).getAnnotationList());
                        }

                    }

                    private void parse(final WebAppContext context, AnnotationParser parser) throws Exception {
                        List<Resource> _resources = getResources(getClass().getClassLoader());

                        for (Resource _resource : _resources) {
                            if (_resource == null)
                                return;

                            parser.clearHandlers();
                            for (DiscoverableAnnotationHandler h : _discoverableAnnotationHandlers) {
                                if (h instanceof AbstractDiscoverableAnnotationHandler)
                                    ((AbstractDiscoverableAnnotationHandler) h).setResource(null); //
                            }
                            parser.registerHandlers(_discoverableAnnotationHandlers);
                            parser.registerHandler(_classInheritanceHandler);
                            parser.registerHandlers(_containerInitializerAnnotationHandlers);

                            parser.parse(_resource,
                                    new ClassNameResolver() {
                                        public boolean isExcluded(String name) {
                                            if (context.isSystemClass(name)) return true;
                                            if (context.isServerClass(name)) return false;
                                            return false;
                                        }

                                        public boolean shouldOverride(String name) {
                                            //looking at webapp classpath, found already-parsed class of same name - did it come from system or duplicate in webapp?
                                            if (context.isParentLoaderPriority())
                                                return false;
                                            return true;
                                        }
                                    });
                        }
                    }

                    private List<Resource> getResources(ClassLoader aLoader) throws IOException {
                        if (aLoader instanceof URLClassLoader) {
                            List<Resource> _result = new ArrayList<Resource>();
                            URL[] _urls = ((URLClassLoader) aLoader).getURLs();
                            for (URL _url : _urls)
                                _result.add(Resource.newResource(_url));

                            return _result;
                        }
                        return Collections.emptyList();
                    }
                }
        });

        List<Handler> list = new ArrayList<Handler>();

        list.add(ctx);

        HandlerList handlerList = new HandlerList();
        handlerList.setHandlers(list.toArray(new Handler[0]));

        RequestLogHandler logHandler = new RequestLogHandler();
        logHandler.setRequestLog(createRequestLog());

        HandlerCollection handler = new HandlerCollection();
        handler.setHandlers(new Handler[]{handlerList, logHandler});

        return handler;
    }

    private RequestLog createRequestLog() {
        NCSARequestLog requestLog = new NCSARequestLog();

        File log = new File(config.getLogDir() + "access.log");
        if (!log.getParentFile().exists())
            log.getParentFile().mkdirs();

        requestLog.setFilename(log.getPath());
        requestLog.setRetainDays(7);
        requestLog.setExtended(false);
        requestLog.setAppend(true);
        requestLog.setLogTimeZone("UTC");
        requestLog.setLogLatency(true);
        return requestLog;
    }
}