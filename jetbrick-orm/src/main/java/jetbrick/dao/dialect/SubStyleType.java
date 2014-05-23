/**
 * Copyright 2013-2014 Guoqiang Chen, Shanghai, China. All rights reserved.
 *
 * Email: subchen@gmail.com
 * URL: http://subchen.github.io/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrick.dao.dialect;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Substyle SQL Type Name.
 * 这个数据库无关的SQL字段类型，可以支持多数据库 
 */
public class SubStyleType {
    protected static final Map<String, Class<?>> javaClassMapping = getJavaClassMapping();

    public static final String UID = "uid"; // integer
    public static final String UUID = "uuid"; // char(16)
    public static final String ENUM = "enum"; // int
    public static final String CHAR = "char";
    public static final String VARCHAR = "varchar";
    public static final String TEXT = "text"; // longvarchar
    public static final String INT = "int";
    public static final String LONG = "long";
    public static final String BIGINT = "bigint";// decimal & numeric
    public static final String DOUBLE = "double";
    public static final String DECIMAL = "decimal";
    public static final String BOOLEAN = "boolean"; // bit
    public static final String DATETIME = "datetime"; // java.sql.Timestamp
    public static final String TIMESTAMP = "timestamp"; //  java.utils.Timestamp
    public static final String DATE = "date"; // java.sql.Date
    public static final String TIME = "time"; // java.sql.Time
    public static final String DATETIME_STRING = "datetime_string"; // char(19), yyyy-MM-dd HH:mm:ss
    public static final String DATE_STRING = "date_string"; // char(10), yyyy-MM-dd
    public static final String TIME_STRING = "time_string"; // char(8), HH:mm:ss
    public static final String CLOB = "clob";
    public static final String BLOB = "blob";
    public static final String BINARY = "binary";
    public static final String VARBINARY = "varbinary";
    public static final String INPUTSTREAM = "inputstream"; // longvarbinary

    public static Class<?> getJavaClass(String typeName) {
        Class<?> clazz = javaClassMapping.get(typeName.toLowerCase());
        return clazz == null ? Object.class : clazz;
    }

    private static Map<String, Class<?>> getJavaClassMapping() {
        Map<String, Class<?>> map = new HashMap<String, Class<?>>();
        map.put(UID, Integer.class);
        map.put(UUID, String.class);
        map.put(ENUM, Integer.class);
        map.put(CHAR, String.class);
        map.put(VARCHAR, String.class);
        map.put(TEXT, String.class);
        map.put(INT, Integer.class);
        map.put(LONG, Long.class);
        map.put(BIGINT, BigInteger.class);
        map.put(DOUBLE, Double.class);
        map.put(DECIMAL, BigDecimal.class);
        map.put(BOOLEAN, Boolean.class);
        map.put(DATETIME, java.util.Date.class);
        map.put(TIMESTAMP, java.sql.Timestamp.class);
        map.put(DATE, java.sql.Date.class);
        map.put(TIME, java.sql.Time.class);
        map.put(DATETIME_STRING, String.class);
        map.put(DATE_STRING, String.class);
        map.put(TIME_STRING, String.class);
        map.put(CLOB, java.sql.Clob.class);
        map.put(BLOB, java.sql.Blob.class);
        map.put(BINARY, byte[].class);
        map.put(VARBINARY, byte[].class);
        map.put(INPUTSTREAM, InputStream.class);
        return map;
    }
}
