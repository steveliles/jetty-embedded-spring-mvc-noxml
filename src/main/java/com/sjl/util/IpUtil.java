package com.sjl.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;

/**
 * ip util
 * User: shijinkui
 * Date: 12-9-3
 * Time: 下午1:50
 * To change this template use File | Settings | File Templates.
 */
public class IpUtil {
    public static String getIp() {
        ArrayList<String> ips = new ArrayList<String>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface interfaceN = (NetworkInterface) interfaces.nextElement();
                Enumeration<InetAddress> ienum = interfaceN.getInetAddresses();
                while (ienum.hasMoreElements()) {
                    // retourne l adresse IPv4 et IPv6
                    String adress = ienum.nextElement().getHostAddress().toString();
                    ips.add(adress);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String localIP = "127.0.0.1";
        String rtv = "";
        if (ips.size() > 0) {
            for (int i = 0; i < ips.size(); i++) {
                String ipTmp = ips.get(i);
                if (ipTmp.equalsIgnoreCase(localIP) || ipTmp.startsWith("220.181.")) {
                    continue;
                } else {
                    rtv = ips.get(i);
                    if (validateIP(rtv))
                        break;
                    else
                        continue;
                }
            }
        }

        return rtv;
    }

    private static boolean validateIP(String ip) {
        boolean rtv = true;
        String digiStr = "0123456789";
        StringTokenizer s = new StringTokenizer(ip, ".");
        while (s.hasMoreElements()) {
            String oneBlock = (String) s.nextElement();

            boolean findOneBlockF = false;
            for (int i = 0; i < oneBlock.length(); i++) {
                String c = "" + oneBlock.charAt(i);
                if (digiStr.indexOf(c) == -1) {
                    findOneBlockF = true;
                    break;
                }
            }

            if (findOneBlockF) {
                rtv = false;
                break;
            }
        }

        return rtv;
    }

    public static String getHostname() {

        String hostName = null;
        try {
            InetAddress addr = InetAddress.getLocalHost();
            hostName = addr.getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return hostName;
    }
}
