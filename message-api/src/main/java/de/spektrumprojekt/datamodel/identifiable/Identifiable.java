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

package de.spektrumprojekt.datamodel.identifiable;

import java.util.UUID;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Now you may ask, why do we have two identifiers? The answer, the id is the "local" one, only
 * valid for the persistence unit used within one component. Once transferred to another
 * component/system the id should be reset or set new. In contrast the globalId is as the name may
 * indicate global, hence should be the same over all systems.
 * 
 * @author Torsten Lunze
 * 
 */
@MappedSuperclass
// @Cache(
// type = CacheType.FULL, // Cache everything until the JVM decides memory is low.
// size = 64000, // Use 64,000 as the initial cache size.
// expiry = -1, // 10 minutes
// coordinationType = CacheCoordinationType.SEND_OBJECT_CHANGES // if cache coordination is
// used, only send invalidation
// messages.)
// )
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "globalId"))
public abstract class Identifiable implements SpektrumEntity {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id = null;

    private String globalId;

    public Identifiable() {
        this("random#" + UUID.randomUUID().toString());
    }

    public Identifiable(String globalId) {
        this.globalId = globalId;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Identifiable other = (Identifiable) obj;
        if (globalId == null) {
            if (other.globalId != null) {
                return false;
            }
        } else if (!globalId.equals(other.globalId)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    public String getGlobalId() {
        return globalId;
    }

    public Long getId() {
        return id;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (globalId == null ? 0 : globalId.hashCode());
        result = prime * result + (id == null ? 0 : id.hashCode());
        return result;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Identifiable [id=");
        builder.append(id);
        builder.append(", globalId=");
        builder.append(globalId);
        builder.append("]");
        return builder.toString();
    }

}
