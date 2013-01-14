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

package de.spektrumprojekt.datamodel.message;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import de.spektrumprojekt.datamodel.identifiable.Identifiable;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
@Entity
public class ScoredTerm extends Identifiable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    // no cascade, the term must be stored before, but if it exits always load it directly
    @ManyToOne(cascade = { }, fetch = FetchType.EAGER, optional = false)
    private Term term;

    private float weight;

    protected ScoredTerm() {
        // constructor only for ORM.
    }

    public ScoredTerm(Term term, float weight) {
        if (term == null) {
            throw new IllegalArgumentException("term cannot be null!");
        }

        this.term = term;
        this.weight = weight;
    }

    public Term getTerm() {
        return term;
    }

    public float getWeight() {
        return weight;
    }

    public void setTerm(Term term) {
        if (term == null) {
            throw new IllegalArgumentException("term cannot be null!");
        }
        this.term = term;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "ScoredTerm [term=" + term + ", weight=" + weight + "]";
    }

}
