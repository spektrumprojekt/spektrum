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

package de.spektrumprojekt.configuration.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SimpleProperties extends DefaultPropertiesConfiguration {

    public SimpleProperties(Map<? extends Object, ? extends Object> properties) {
        super(properties);
    }

    public SimpleProperties(String filename) throws IOException {
        super(null);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File(filename));
            getDefaultProperties().load(fis);
        } finally {
            fis.close();
        }
    }

    @Override
    public boolean internalExistsProperty(String key) {
        return getDefaultProperties().contains(key);
    }

    @Override
    protected int internalGetIntProperty(String key) {
        // it is a little awkward, but this implementation uses the default properties of the super
        // class. so it should have found it already, it only calls this if exists is false, so we
        // know, it does not exist.
        return 0;
    }

    @Override
    protected String internalGetStringProperty(String key) {
        // it is a little awkward, but this implementation uses the default properties of the super
        // class. so it should have found it already, it only calls this if exists is false, so we
        // know, it does not exist.
        return null;
    }

    @Override
    protected List<String> internalGetListProperty(String key) {
        // it is a little awkward, but this implementation uses the default properties of the super
        // class. so it should have found it already, it only calls this if exists is false, so we
        // know, it does not exist.
        return null;
    }

}
