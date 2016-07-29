/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package org.komodo.rest.relational.json;

import static org.komodo.rest.relational.json.KomodoJsonMarshaller.BUILDER;
import java.io.IOException;
import org.komodo.rest.relational.response.RestDataSourceDriver;
import org.komodo.rest.relational.response.RestTeiidStatus;
import org.komodo.utils.StringUtils;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class TeiidStatusSerializer extends BasicEntitySerializer<RestTeiidStatus> {

    @Override
    protected boolean isComplete(final RestTeiidStatus teiid) {
        return super.isComplete(teiid) && !StringUtils.isBlank(teiid.getConnectionUrl());
    }

    @Override
    protected RestTeiidStatus createEntity() {
        return new RestTeiidStatus();
    }

    @Override
    protected String readExtension(String name, RestTeiidStatus status, JsonReader in) {
        if (RestTeiidStatus.TEIID_DATA_SOURCE_DRIVERS_LABEL.equals(name)) {
            RestDataSourceDriver[] drivers = BUILDER.fromJson(in, RestDataSourceDriver[].class);
            status.setDataSourceDrivers(drivers);
            return name;
        }

        return null;
    }

    @Override
    protected void writeExtensions(JsonWriter out, RestTeiidStatus entity) throws IOException {
        out.name(RestTeiidStatus.TEIID_DATA_SOURCE_DRIVERS_LABEL);
        BUILDER.toJson(entity.getDataSourceDrivers().toArray(new RestDataSourceDriver[0]), RestDataSourceDriver[].class, out);
    }
}
