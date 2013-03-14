package com.sjl.config;

import com.google.common.base.Optional;
import com.sjl.util.Duration;
import com.sjl.util.Size;

/**
 * Created with IntelliJ IDEA.
 * User=shijinkui
 * Date=13-3-13
 * Time=下午11:22
 * To change this template use File | Settings | File Templates.
 */
public class ServerConfig {

    private String name;
    private int port;
    private int adminPort;
    private String ip;
    private int minThreads;
    private int maxThreads;
    private String logDir;

    //default http config
    private final boolean useForwardedHeaders = true;
    //The maximum amount of time a connection is allowed to be idle before being closed.
    private final Duration maxIdleTime = Duration.seconds(10);

    //The number of threads dedicated to accepting connections. If omitted, this defaults to the
    //number of logical CPUs on the current machine.
    private final int acceptorThreads = 3;

    // The number of unaccepted requests to keep in the accept queue before refusing connections. If
    // set to -1 or omitted, the system default is used.
    private final int acceptQueueSize = 100;
    // The maximum number of buffers to keep in memory.
    private final int maxBufferCount = 2048;
    // The initial buffer size for reading requests.
    private final Size requestBufferSize = Size.kilobytes(8);
    private final Size requestHeaderBufferSize = Size.kilobytes(2);
    private final Size responseBufferSize = Size.kilobytes(16);
    private final Size responseHeaderBufferSize = Size.kilobytes(1);
    private final boolean reuseAddress = true;
    private final Duration soLingerTime = Duration.seconds(1);
    private final int lowResourcesConnectionThreshold = 25000;
    private final Duration lowResourcesMaxIdleTime = Duration.seconds(1);
    private final Duration shutdownGracePeriod = Duration.seconds(2);
    private final boolean useServerHeader = false;
    private final boolean useDirectBuffers = true;
    private final int acceptorThreadPriorityOffset = 0;


    public void init(String name, String ip, int port, int minThreads, int maxThreads) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.minThreads = minThreads;
        this.maxThreads = maxThreads;
        this.logDir = System.getProperty("server_log_home", String.format("./var/logs/$s/", name));
    }

    public String getName() {
        return Optional.of(name).or("demo-test");
    }

    public int getPort() {
        return Optional.of(port).or(8080);
    }

    public String getIp() {
        return Optional.of(ip).or("127.0.0.1");
    }

    public int getMinThreads() {
        return Optional.of(minThreads).or(5);
    }

    public int getMaxThreads() {
        return Optional.of(maxThreads).or(128);
    }

    public String getLogDir() {
        return logDir;
    }

    public boolean isUseForwardedHeaders() {
        return useForwardedHeaders;
    }

    public Duration getMaxIdleTime() {
        return maxIdleTime;
    }

    public int getAcceptorThreads() {
        return acceptorThreads;
    }

    public int getAcceptQueueSize() {
        return acceptQueueSize;
    }

    public int getMaxBufferCount() {
        return maxBufferCount;
    }

    public Size getRequestBufferSize() {
        return requestBufferSize;
    }

    public Size getRequestHeaderBufferSize() {
        return requestHeaderBufferSize;
    }

    public Size getResponseBufferSize() {
        return responseBufferSize;
    }

    public Size getResponseHeaderBufferSize() {
        return responseHeaderBufferSize;
    }

    public boolean isReuseAddress() {
        return reuseAddress;
    }

    public Optional<Duration> getSoLingerTime() {
        return Optional.fromNullable(soLingerTime);
    }

    public int getLowResourcesConnectionThreshold() {
        return lowResourcesConnectionThreshold;
    }

    public Duration getLowResourcesMaxIdleTime() {
        return lowResourcesMaxIdleTime;
    }

    public Duration getShutdownGracePeriod() {
        return shutdownGracePeriod;
    }

    public boolean isUseServerHeader() {
        return useServerHeader;
    }

    public boolean isUseDirectBuffers() {
        return useDirectBuffers;
    }

    public int getAcceptorThreadPriorityOffset() {
        return acceptorThreadPriorityOffset;
    }

    public int getAdminPort() {
        return Optional.of(adminPort).or(getPort() + 1);
    }
}