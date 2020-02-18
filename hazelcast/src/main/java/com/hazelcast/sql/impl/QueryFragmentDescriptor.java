/*
 * Copyright (c) 2008-2020, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.sql.impl;

import com.hazelcast.internal.util.UUIDSerializationUtil;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.sql.impl.physical.PhysicalNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Query fragment descriptor which is sent over wire.
 */
public class QueryFragmentDescriptor implements DataSerializable {
    /** Physical node. */
    private PhysicalNode node;

    /** IDs of mapped nodes. May be null if nodes could be inherited from the context. */
    private List<UUID> mappedMemberIds;

    public QueryFragmentDescriptor() {
        // No-op.
    }

    public QueryFragmentDescriptor(
        PhysicalNode node,
        List<UUID> mappedMemberIds
    ) {
        this.node = node;
        this.mappedMemberIds = mappedMemberIds;
    }

    public PhysicalNode getNode() {
        return node;
    }

    public List<UUID> getMappedMemberIds() {
        return mappedMemberIds != null ? mappedMemberIds : Collections.emptyList();
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeObject(node);

        List<UUID> mappedMemberIds0 = getMappedMemberIds();

        out.writeInt(mappedMemberIds0.size());

        for (UUID mappedMemberId : mappedMemberIds0) {
            UUIDSerializationUtil.writeUUID(out, mappedMemberId);
        }
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        node = in.readObject();

        int mappedMemberIdsSize = in.readInt();

        if (mappedMemberIdsSize > 0) {
            mappedMemberIds = new ArrayList<>(mappedMemberIdsSize);

            for (int i = 0; i < mappedMemberIdsSize; i++) {
                mappedMemberIds.add(UUIDSerializationUtil.readUUID(in));
            }
        }
    }

    /**
     * Get members participating in the given fragment.
     *
     * @param dataMemberIds Data member IDs.
     * @return Members participating in the given fragment.
     */
    public Collection<UUID> getFragmentMembers(Collection<UUID> dataMemberIds) {
        if (mappedMemberIds != null) {
            return mappedMemberIds;
        } else {
            return dataMemberIds;
        }
    }
}