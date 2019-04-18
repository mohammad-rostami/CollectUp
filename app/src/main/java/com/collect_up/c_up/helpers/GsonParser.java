/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.helpers;

import com.google.gson.Gson;

/**
 * Contains static methods to perform operations with Gson.
 */
public class GsonParser {
    /**
     * Convert JSON string to objects
     *
     * @param response JSON string in bytes
     * @param clazz    Model to be used with Gson
     * @param <T>      The model
     * @return An array of objects based on the T model
     */
    public static <T> T[] getArrayFromGson(byte[] response, Class<T[]> clazz) {
        try {
            return new Gson().fromJson(new String(response), clazz);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Convert JSON string to one object
     *
     * @param response JSON string in bytes
     * @param clazz    Model to be used with Gson
     * @param <T>      The model
     * @return An object based on the T model
     */
    public static <T> T getObjectFromGson(byte[] response, Class<T> clazz) {
        return new Gson().fromJson(new String(response), clazz);
    }
}
