/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
* 
* http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package de.spektrumprojekt.commons;

import java.util.ArrayList;
import java.util.List;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

/**
 * <p>
 * Utility functions for SPEKTRUM.
 * </p>
 * 
 * @author Philipp Katz
 */
public final class SpektrumUtils {

    public static <T> T deserializeJson(String json) {
        JSONDeserializer<T> deserializer = new JSONDeserializer<T>();
        return deserializer.deserialize(json);
    }

    public static <T> T getFirst(List<T> list) {
        T result = null;
        if (list.size() > 0) {
            result = list.get(0);
        }
        return result;
    }

    public static <T> List<T> getSublist(List<T> list, int size) {
        List<T> result = new ArrayList<T>(list);
        if (result.size() > size) {
            result = result.subList(0, size);
        }
        return result;
    }

    public static boolean notNull(Object... objects) {
        for (Object object : objects) {
            if (object == null) {
                return false;
            }
        }
        return true;
    }

    public static boolean notNullOrEmpty(String string) {
        if (string == null) {
            return false;
        }
        if (string.isEmpty()) {
            return false;
        }
        return true;
    }

    public static boolean nullOrEmpty(String string) {
        return !notNullOrEmpty(string);
    }

    public static String serializeJson(Object object) {
        JSONSerializer serializer = new JSONSerializer();
        return serializer.deepSerialize(object);
    }

    public static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private SpektrumUtils() {
        // prevent instantiation.
    }

}
