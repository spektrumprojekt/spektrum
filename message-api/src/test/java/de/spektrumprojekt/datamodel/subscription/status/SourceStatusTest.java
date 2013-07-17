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

package de.spektrumprojekt.datamodel.subscription.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import de.spektrumprojekt.datamodel.source.Source;
import de.spektrumprojekt.datamodel.source.SourceStatus;

public class SourceStatusTest {

    @Test
    public void testUpdateCheck() {

        Source source = new Source("connectorType");
        SourceStatus status = new SourceStatus(source);

        status.updateCheck(StatusType.OK);

        assertEquals(StatusType.OK, status.getLastStatusType());
        assertEquals(0, (int) status.getConsecutiveErrorCount());
        assertEquals(0, (int) status.getErrorCount());
        assertNull(status.getLastError());
        assertNotNull(status.getLastSuccessfulCheck());

        status.updateCheck(StatusType.ERROR_UNSPECIFIED);

        assertEquals(StatusType.ERROR_UNSPECIFIED, status.getLastStatusType());
        assertEquals(1, (int) status.getConsecutiveErrorCount());
        assertEquals(1, (int) status.getErrorCount());
        assertNotNull(status.getLastError());
        assertNotNull(status.getLastSuccessfulCheck());
    }
}
