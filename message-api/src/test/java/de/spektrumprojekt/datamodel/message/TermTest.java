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

import java.util.Collection;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

import de.spektrumprojekt.datamodel.message.Term.TermCategory;

/**
 * Testing some simple stuff
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class TermTest {

    /**
     * Test the terms
     */
    @Test
    public void testMessageGroup() {
        final String test = "test";
        final MessageGroup mg = new MessageGroup();
        mg.setId(13l);

        String mgValue = Term.getMessageGroupSpecificTermValue(mg, test);
        Term mgTerm = new Term(TermCategory.TERM, mgValue);
        Term nonMgTerm = new Term(TermCategory.TERM, test);

        Assert.assertEquals(mg.getId(), mgTerm.getMessageGroupId());
        Assert.assertNull(nonMgTerm.getMessageGroupId());
    }

    /**
     * Test the terms
     */
    @Test
    public void testUniqueness() {
        Collection<Term> terms = new HashSet<Term>();

        Term term1 = new Term(TermCategory.TERM, "term1");
        Term term2 = new Term(TermCategory.TERM, "term2");
        Term term3 = new Term(TermCategory.TERM, "term3");

        Assert.assertFalse(term1.equals(term2));
        Assert.assertFalse(term2.equals(term1));

        Assert.assertFalse(term1.equals(term3));
        Assert.assertFalse(term3.equals(term1));

        Assert.assertFalse(term2.equals(term3));
        Assert.assertFalse(term3.equals(term2));

        Assert.assertTrue(terms.add(term1));
        Assert.assertTrue(terms.add(term2));
        Assert.assertTrue(terms.add(term3));
    }
}
