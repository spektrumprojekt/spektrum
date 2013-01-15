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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class KeyphraseCandidates implements Iterable<KeyphraseCandidate> {

    final class CandidateLengthComparator implements Comparator<KeyphraseCandidate> {
        @Override
        public int compare(KeyphraseCandidate c1, KeyphraseCandidate c2) {
            return Integer.valueOf(c1.getStemmedValue().length()).compareTo(
                    c2.getStemmedValue().length());
        }
    }

    private final Map<String, KeyphraseCandidate> candidates;

    public KeyphraseCandidates() {
        this.candidates = new HashMap<String, KeyphraseCandidate>();
    }

    public void addCandidate(String stemmedValue, String unstemmedValue, int count) {
        KeyphraseCandidate candidate = candidates.get(stemmedValue);
        if (candidate == null) {
            candidate = new KeyphraseCandidate(stemmedValue, unstemmedValue, count);
            candidates.put(stemmedValue, candidate);
        } else {
            candidate.add(unstemmedValue, count);
        }
    }

    public KeyphraseCandidate getCandidate(String stemmedValue) {
        return candidates.get(stemmedValue);
    }

    @Override
    public Iterator<KeyphraseCandidate> iterator() {
        return candidates.values().iterator();
    }

    /**
     * <p>
     * Remove overlapping phrases, e.g. we extracted the 2-gram "San Francisco" three times and the
     * 1-gram "Francisco" four times, the count of "Francisco" is decremented to one, as it is
     * contained in the longer gram "San Francisco" which occurs three times.
     * </p>
     * 
     * @param candidates
     * @return
     */
    public void removeOverlaps() {
        List<KeyphraseCandidate> temp = new ArrayList<KeyphraseCandidate>(candidates.values());
        Collections.sort(temp, new CandidateLengthComparator());
        for (int i = 0; i < temp.size(); i++) {
            KeyphraseCandidate current = temp.get(i);
            int highestOverlap = 0;
            for (int j = i + 1; j < temp.size(); j++) {
                KeyphraseCandidate other = temp.get(j);
                if (other.getStemmedValue().contains(current.getStemmedValue())) {
                    highestOverlap = Math.max(highestOverlap, other.getCount());
                }
            }
            if (highestOverlap > 0) {
                current.decrementCount(highestOverlap);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (KeyphraseCandidate cand : this) {
            stringBuilder.append(cand).append(",");
        }
        return stringBuilder.toString();
    }

}
