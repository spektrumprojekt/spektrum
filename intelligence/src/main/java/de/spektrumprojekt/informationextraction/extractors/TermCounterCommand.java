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

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.i.informationextraction.frequency.TermFrequencyComputer;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;

public final class TermCounterCommand implements Command<InformationExtractionContext> {

    private final TermFrequencyComputer termFrequencyComputer;

    public TermCounterCommand(TermFrequencyComputer termFrequencyComputer) {
        if (termFrequencyComputer == null) {
            throw new IllegalArgumentException("termFrequencyComputer cannot be null!");
        }
        this.termFrequencyComputer = termFrequencyComputer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " termFrequencyComputer: "
                + termFrequencyComputer.getConfigurationDescription();
    }

    @Override
    public void process(InformationExtractionContext context) {

        termFrequencyComputer.integrate(context.getMessage());

    }

}
