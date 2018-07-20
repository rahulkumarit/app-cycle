package com.techzellent.hicycle.wsCalling;

public class WSUtils {
    //constant values
    public static final int STATION_WS_CODE = 1001;
    public static final int LOGIN_WS_CODE = 1002;
    //url
    public static final String BASE_URL = " http://dev.pushkarbicycle.co.in/";
    public static final String WS_LOGIN = BASE_URL + "api/bicycle/login";
    public static final String WS_STATIONS = BASE_URL + "api/bicycle/stations";
    public static final String WS_REGISTER = BASE_URL + "api/Account/Register";
    public static final String WS_ASSIGN = BASE_URL + "api/bicycle/assign";
    public static final String WS_UNLOCK = BASE_URL + "api/bicycle/unlock";
    public static final String WS_ENDTRIP = BASE_URL + "api/bicycle/endtrip";
}
