package com.example.network;

public class BaseConfig {
    public static final boolean DEBUG;

    static {
        Boolean.parseBoolean("true");
        DEBUG = true;
    }
    public static final String APPLICATION_ID = "com.sensetime.sdk.video.android";
    public static final String BUILD_TYPE = "debug";
    public static final int VERSION_CODE = 1;
    public static final String VERSION_NAME = "1.4.4";
    private String BASE_URL;

    public String getBASE_URL() {
        return BASE_URL;
    }

    public void setBASE_URL(String BASE_URL) {
        this.BASE_URL = BASE_URL;
    }
}
