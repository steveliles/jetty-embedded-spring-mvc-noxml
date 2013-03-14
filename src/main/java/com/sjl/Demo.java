package com.sjl;

import com.sjl.config.ServerConfig;
import com.sjl.util.IpUtil;

public class Demo extends AbstractWebServer {

    public static void main(String... args) throws Exception {
        final AbstractWebServer server = new Demo();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    System.out.println(" server shutdown");
                    server.shutdown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        server.run(args);
    }


    @Override
    public void initialize(ServerConfig config) {
        config.init("ServerTest", IpUtil.getIp(), 8000, 10, 50);
    }
}
