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

package de.spektrumprojekt.datamodel.observation;

/**
 * The observed interest
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public enum Interest {
    /**
     * Extreme
     */
    EXTREME(1f),
    /**
     * high
     */
    HIGH(0.75f),
    /**
     * normal
     */
    NORMAL(0.5f),
    /**
     * low
     */
    LOW(0.25f),
    /**
     * none
     */
    NONE(0f);

    private float score;

    /**
     * 
     * @param score
     *            the score related to the inte
     */
    private Interest(float score) {
        this.score = score;
    }

    /**
     * 
     * @return the score
     */
    public float getScore() {
        return score;
    }
}
