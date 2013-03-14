package com.sjl.http;

import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: shijinkui
 * Date: 13-3-14
 * Time: 上午12:38
 * To change this template use File | Settings | File Templates.
 */
public enum ConnectorType {
    BLOCKING,
    LEGACY,
    LEGACY_SSL,
    NONBLOCKING,
    NONBLOCKING_SSL;

    @Override
    public String toString() {
        return super.toString().replace("_", "+").toLowerCase(Locale.ENGLISH);
    }

    public static ConnectorType parse(String type) {
        return valueOf(type.toUpperCase(Locale.ENGLISH).replace('+', '_'));
    }
}