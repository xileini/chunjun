/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dtstack.flinkx.converter;

import org.apache.flink.table.data.RowData;
import org.apache.flink.table.types.logical.LogicalType;
import org.apache.flink.table.types.logical.RowType;

import java.io.Serializable;
import java.sql.ResultSet;

import static org.apache.flink.util.Preconditions.checkNotNull;

/**
 * Converter that is responsible to convert between JDBC object and Flink SQL internal data
 * structure {@link RowData}.
 */

/**
 * @program: flinkx
 * @author: wuren
 * @create: 2021/04/10
 **/
public abstract class AbstractRowConverter<SourceT, LookupT, SinkT> implements Serializable {

    protected final RowType rowType;
    protected final DeserializationConverter[] toInternalConverters;
    protected final SerializationConverter[] toExternalConverters;
    protected final LogicalType[] fieldTypes;

    public AbstractRowConverter(RowType rowType) {
        this.rowType = checkNotNull(rowType);
        this.fieldTypes =
                rowType.getFields().stream()
                        .map(RowType.RowField::getType)
                        .toArray(LogicalType[]::new);
        this.toInternalConverters = new DeserializationConverter[rowType.getFieldCount()];
        this.toExternalConverters = new SerializationConverter[rowType.getFieldCount()];
        for (int i = 0; i < rowType.getFieldCount(); i++) {
            toInternalConverters[i] = createNullableInternalConverter(rowType.getTypeAt(i));
            toExternalConverters[i] = createNullableExternalConverter(fieldTypes[i]);
        }
    }

    /**
     * Create a nullable runtime {@link DeserializationConverter} from given {@link
     * LogicalType}.
     */
    protected DeserializationConverter createNullableInternalConverter(LogicalType type) {
        return wrapIntoNullableInternalConverter(createInternalConverter(type));
    }

    protected DeserializationConverter wrapIntoNullableInternalConverter(
            DeserializationConverter deserializationConverter) {
        return val -> {
            if (val == null) {
                return null;
            } else {
                return deserializationConverter.deserialize(val);
            }
        };
    }

    /** Create a nullable JDBC f{@link SerializationConverter} from given sql type. */
    protected SerializationConverter createNullableExternalConverter(LogicalType type) {
        return wrapIntoNullableExternalConverter(createExternalConverter(type), type);
    }

    protected abstract SerializationConverter wrapIntoNullableExternalConverter(
            SerializationConverter serializationConverter, LogicalType type);

    /**
     * Convert data retrieved from {@link ResultSet} to internal {@link RowData}.
     *
     * @param input from JDBC
     */
    public abstract RowData toInternal(SourceT input) throws Exception;

    public abstract RowData toInternalLookup(LookupT input) throws Exception;

    /**
     * Convert data retrieved from Flink internal RowData to Object or byte[] etc.
     *
     * @param rowData The given internal {@link RowData}.
     *
     * @return The filled statement.
     */
    public abstract SinkT toExternal(RowData rowData, SinkT output) throws Exception;

    /** Runtime converter to convert field to {@link RowData} type object. */
    @FunctionalInterface
    protected interface DeserializationConverter<T> extends Serializable {
        Object deserialize(T field) throws Exception;
    }

    /**
     * 类型T一般是 Object，HBase这种特殊的就是byte[]
     *
     * @param <T>
     */
    @FunctionalInterface
    protected interface SerializationConverter<T> extends Serializable {
        void serialize(RowData rowData, int pos, T output) throws Exception;
    }

    /**
     * 先调用这个
     *
     * @param type
     *
     * @return
     */
    protected abstract DeserializationConverter createInternalConverter(LogicalType type);

    /**
     * 先调用这个
     *
     * @param type
     *
     * @return
     */
    protected abstract SerializationConverter createExternalConverter(LogicalType type);
}