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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    VERY_HIGH(0.875f),

    /**
     * high
     */
    HIGH(0.75f),

    ABOVE_NORMAL(0.625f),
    /**
     * normal
     */
    NORMAL(0.5f),

    BELOW_NORMAL(0.375f),

    /**
     * low
     */
    LOW(0.25f),

    VERY_LOW(0.125f),
    /**
     * none
     */
    NONE(0f);

    private float score;

    /**
     * Unmodifiable list with all interests, starting with the lowest.
     */
    public static final List<Interest> INTERESTS_ORDERED = Collections.unmodifiableList(Arrays
            .asList(new Interest[] {
                    NONE,
                    VERY_LOW,
                    LOW,
                    BELOW_NORMAL,
                    NORMAL,
                    ABOVE_NORMAL,
                    HIGH,
                    VERY_HIGH, EXTREME
            }));

    /**
     * Get the nearest interest to the given score.
     * 
     * @param score
     * @return
     */
    public static Interest match(float score) {
        Interest interest = Interest.EXTREME;
        for (int i = 0; i < INTERESTS_ORDERED.size() - 1; i++) {
            Interest in = INTERESTS_ORDERED.get(i);
            Interest inNext = INTERESTS_ORDERED.get(i + 1);
            if (score <= in.getScore()) {
                interest = in;
                break;
            } else if (score <= inNext.getScore()) {
                float diff = inNext.getScore() - in.getScore();
                float splitPoint = in.getScore() + diff / 2;
                interest = score < splitPoint ? in : inNext;
                break;
            }

        }
        return interest;
    }

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
