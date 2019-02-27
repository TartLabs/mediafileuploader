package com.tartlabs.mediafileupload.util;

import com.google.gson.Gson;

public class ConversionUtils {
    private static Gson gson = GsonWrapper.newInstance();

    public static <T> String getStringFromJson(Object service) {
        return gson.toJson(service);
    }

    public static <T> T getJsonFromString(String response, Class<T> service) {
        return gson.fromJson(response, service);
    }
}