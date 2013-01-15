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

package de.spektrumprojekt.informationextraction.extractors;

import java.util.Set;

/**
 * <p>
 * Simple implementation of a Bag data structure. A bag is similar to a {@link Set} but keeps a
 * count how often an identical item was added.
 * </p>
 * 
 * 
 */
public final class BagHelper {

    /**
     * 
     * @param bag
     *            the bag
     * @return the highest count that exists within the bag
     */
    public static int getHighestCount(org.apache.commons.collections.Bag bag) {
        int highestCount = 0;
        for (Object item : bag) {
            highestCount = Math.max(highestCount, bag.getCount(item));
        }
        return highestCount;
    }

    /**
     * 
     * @param bag
     *            the bag to check
     * @return the item that occurs most often in the bag
     */
    public static Object getHighestItem(org.apache.commons.collections.Bag bag) {
        Object highestItem = null;
        int highestCount = 0;
        for (Object item : bag) {
            int itemCount = bag.getCount(item);
            if (itemCount > highestCount) {
                highestItem = item;
                highestCount = itemCount;
            }
        }
        return highestItem;
    }

    /**
     * Helper class
     */
    private BagHelper() {
        // do not construct me
    }
}
