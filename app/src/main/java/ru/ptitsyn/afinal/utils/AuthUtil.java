package ru.ptitsyn.afinal.utils;

import java.util.HashMap;
import java.util.Map;

public class AuthUtil {

    public static String token = "Token 89b59e5094074b7c97f72453bf1d412d";
    public static String domain = "https://zvukislov.ru";

    public static Map<String, String> headers() {
        Map<String, String> headers =  new HashMap<String, String>();
        headers.put("Authorization", token);
        return headers;
    }
}
