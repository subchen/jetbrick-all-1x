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
package jetbrick.dao.orm.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import jetbrick.beans.ClassUtils;

public class ResultSetGetter {

    public static <T> T getValue(ResultSet rs, String name, Class<T> requiredType) throws SQLException {
        return getValue(rs, rs.findColumn(name), requiredType);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getValue(ResultSet rs, int index, Class<T> requiredType) throws SQLException {
        Object value = null;
        boolean wasNullCheck = false;

        if (requiredType == null) {
            return (T) rs.getObject(index);
        }

        requiredType = (Class<T>) ClassUtils.primitiveToWrapper(requiredType);

        // Explicitly extract typed value, as far as possible.
        if (String.class.equals(requiredType)) {
            value = rs.getString(index);
        } else if (Integer.class.equals(requiredType)) {
            value = Integer.valueOf(rs.getInt(index));
            wasNullCheck = true;
        } else if (Double.class.equals(requiredType)) {
            value = new Double(rs.getDouble(index));
            wasNullCheck = true;
        } else if (Boolean.class.equals(requiredType)) {
            value = (rs.getBoolean(index) ? Boolean.TRUE : Boolean.FALSE);
            wasNullCheck = true;
        } else if (java.sql.Date.class.equals(requiredType)) {
            value = rs.getDate(index);
        } else if (java.sql.Time.class.equals(requiredType)) {
            value = rs.getTime(index);
        } else if (java.sql.Timestamp.class.equals(requiredType)) {
            value = rs.getTimestamp(index);
        } else if (java.util.Date.class.equals(requiredType)) {
            value = new java.util.Date(rs.getTimestamp(index).getTime());
        } else if (Byte.class.equals(requiredType)) {
            value = Byte.valueOf(rs.getByte(index));
            wasNullCheck = true;
        } else if (Short.class.equals(requiredType)) {
            value = Short.valueOf(rs.getShort(index));
            wasNullCheck = true;
        } else if (Long.class.equals(requiredType)) {
            value = Long.valueOf(rs.getLong(index));
            wasNullCheck = true;
        } else if (Float.class.equals(requiredType)) {
            value = new Float(rs.getFloat(index));
            wasNullCheck = true;
        } else if (Number.class.equals(requiredType)) {
            value = new Double(rs.getDouble(index));
            wasNullCheck = true;
        } else if (byte[].class.equals(requiredType)) {
            value = rs.getBytes(index);
        } else if (java.math.BigDecimal.class.equals(requiredType)) {
            value = rs.getBigDecimal(index);
        } else if (java.sql.Blob.class.equals(requiredType)) {
            value = rs.getBlob(index);
        } else if (java.sql.Clob.class.equals(requiredType)) {
            value = rs.getClob(index);
        } else if (java.net.URL.class.equals(requiredType)) {
            value = rs.getURL(index);
        } else {
            // Some unknown type desired -> rely on getObject.
            value = rs.getObject(index);
        }

        // Perform was-null check if demanded (for results that the
        // JDBC driver returns as primitives).
        if (wasNullCheck && value != null && rs.wasNull()) {
            value = null;
        }
        return (T) value;
    }

}
