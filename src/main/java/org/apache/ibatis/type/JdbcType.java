/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.type;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * JDBC类型枚举,
 * <p>
 * 把java.sql.Types中的int常量包装成枚举类型, 更安全了
 */
public enum JdbcType {
    /*
     * This is added to enable basic support for the
     * ARRAY data type - but a custom type handler is still required
     */
    //就是包装一下java.sql.Types
    ARRAY(Types.ARRAY),//
    BIT(Types.BIT), //
    TINYINT(Types.TINYINT),//
    SMALLINT(Types.SMALLINT),//
    INTEGER(Types.INTEGER), //
    BIGINT(Types.BIGINT), //
    FLOAT(Types.FLOAT), //
    REAL(Types.REAL),//
    DOUBLE(Types.DOUBLE),//
    NUMERIC(Types.NUMERIC), //
    DECIMAL(Types.DECIMAL),//
    CHAR(Types.CHAR),//
    VARCHAR(Types.VARCHAR),//
    LONGVARCHAR(Types.LONGVARCHAR),//
    DATE(Types.DATE), TIME(Types.TIME),//
    TIMESTAMP(Types.TIMESTAMP), //
    BINARY(Types.BINARY),//
    VARBINARY(Types.VARBINARY),//
    LONGVARBINARY(Types.LONGVARBINARY),//
    NULL(Types.NULL),//
    OTHER(Types.OTHER),//
    BLOB(Types.BLOB), CLOB(Types.CLOB),//
    BOOLEAN(Types.BOOLEAN),//
    CURSOR(-10), // Oracle
    UNDEFINED(Integer.MIN_VALUE + 1000), //太周到了，还考虑jdk5兼容性，jdk6的常量都不是直接引用
    NVARCHAR(Types.NVARCHAR), // JDK6
    NCHAR(Types.NCHAR), // JDK6
    NCLOB(Types.NCLOB), // JDK6
    STRUCT(Types.STRUCT);
    /** the SQL type code defined in java.sql.Types, Types这个类全是常量 */
    public final int TYPE_CODE;
    /**
     * Map<the SQL type code defined in java.sql.Types, JdbcType枚举对象>
     * <p>
     * codeLookup就是一个缓存:
     * key=the SQL type code defined in java.sql.Types
     * value=JdbcType的枚举常量
     */
    private static Map<Integer, JdbcType> codeLookup = new HashMap<Integer, JdbcType>();

    //一开始就将数字对应的枚举型放入hashmap
    static {
        for (JdbcType type : JdbcType.values()) {
            codeLookup.put(type.TYPE_CODE, type);
        }
    }

    /**
     * @param code the SQL type code defined in java.sql.Types
     * @see Types
     */
    JdbcType(int code) {
        this.TYPE_CODE = code;
    }

    /**
     * 把java.sql.Types的int常量映射成JdbcType的枚举常量
     *
     * @param code the SQL type code defined in {@link Types}
     * @return code对应的JdbcType
     */
    public static JdbcType forCode(int code) {
        return codeLookup.get(code);
    }

}
