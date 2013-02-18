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

import java.util.Comparator;

public class ObservationDateComparator implements Comparator<Observation> {

    private final boolean descending;

    public ObservationDateComparator() {
        this(false);
    }

    public ObservationDateComparator(boolean descending) {
        this.descending = descending;
    }

    @Override
    public int compare(Observation o1, Observation o2) {

        int result = o1.getObservationDate().compareTo(o2.getObservationDate());
        if (descending) {
            result = -1 * result;
        }
        return result;
    }

}