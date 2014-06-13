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
package jetbrick.dao.jdbclog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Wrapper to do logging
 */
public class JdbcLogSupport {
    protected static final Logger log = LoggerFactory.getLogger(JdbcLogSupport.class);
    protected static final Set<String> SET_METHODS = new HashSet<String>();
    protected static final Set<String> GET_METHODS = new HashSet<String>();
    protected static final Set<String> UPDATE_METHODS = new HashSet<String>();
    protected static final Set<String> MOVE_METHODS = new HashSet<String>();
    protected static final Set<String> EXECUTE_METHODS = new HashSet<String>();
    protected static final Set<String> RESULTSET_METHODS = new HashSet<String>();
    protected static final AtomicInteger idGenerated = new AtomicInteger(1000);
    
    protected final int id = idGenerated.getAndIncrement();
    protected final List<Object> paramNameList = new ArrayList<Object>();
    protected final List<Object> paramValueList = new ArrayList<Object>();
    protected final List<String> paramTypeList = new ArrayList<String>();

    static {
        SET_METHODS.add("setString");
        SET_METHODS.add("setInt");
        SET_METHODS.add("setLong");
        SET_METHODS.add("setShort");
        SET_METHODS.add("setByte");
        SET_METHODS.add("setFloat");
        SET_METHODS.add("setBoolean");
        SET_METHODS.add("setDouble");
        SET_METHODS.add("setDate");
        SET_METHODS.add("setTime");
        SET_METHODS.add("setTimestamp");
        SET_METHODS.add("setBytes");
        SET_METHODS.add("setBigDecimal");
        SET_METHODS.add("setAsciiStream");
        SET_METHODS.add("setUnicodeStream");
        SET_METHODS.add("setCharacterStream");
        SET_METHODS.add("setBinaryStream");
        SET_METHODS.add("setBlob");
        SET_METHODS.add("setClob");
        SET_METHODS.add("setArray");
        SET_METHODS.add("setUrl");
        SET_METHODS.add("setRef");
        SET_METHODS.add("setNString");
        SET_METHODS.add("setNClob");
        SET_METHODS.add("setNCharacterStream");
        SET_METHODS.add("setSQLXML");
        SET_METHODS.add("setNull");
        SET_METHODS.add("setObject");

        GET_METHODS.add("getString");
        GET_METHODS.add("getInt");
        GET_METHODS.add("getLong");
        GET_METHODS.add("getShort");
        GET_METHODS.add("getByte");
        GET_METHODS.add("getFloat");
        GET_METHODS.add("getBoolean");
        GET_METHODS.add("getDouble");
        GET_METHODS.add("getDate");
        GET_METHODS.add("getTime");
        GET_METHODS.add("getTimestamp");
        GET_METHODS.add("getBytes");
        GET_METHODS.add("getBigDecimal");
        GET_METHODS.add("getAsciiStream");
        GET_METHODS.add("getUnicodeStream");
        GET_METHODS.add("getCharacterStream");
        GET_METHODS.add("getBinaryStream");
        GET_METHODS.add("getBlob");
        GET_METHODS.add("getClob");
        GET_METHODS.add("getArray");
        GET_METHODS.add("getUrl");
        GET_METHODS.add("getRef");
        GET_METHODS.add("getNString");
        GET_METHODS.add("getNClob");
        GET_METHODS.add("getNCharacterStream");
        GET_METHODS.add("getSQLXML");
        GET_METHODS.add("getNull");
        GET_METHODS.add("getObject");

        UPDATE_METHODS.add("updateString");
        UPDATE_METHODS.add("updateInt");
        UPDATE_METHODS.add("updateLong");
        UPDATE_METHODS.add("updateShort");
        UPDATE_METHODS.add("updateByte");
        UPDATE_METHODS.add("updateFloat");
        UPDATE_METHODS.add("updateBoolean");
        UPDATE_METHODS.add("updateDouble");
        UPDATE_METHODS.add("updateDate");
        UPDATE_METHODS.add("updateTime");
        UPDATE_METHODS.add("updateTimestamp");
        UPDATE_METHODS.add("updateBytes");
        UPDATE_METHODS.add("updateBigDecimal");
        UPDATE_METHODS.add("updateAsciiStream");
        UPDATE_METHODS.add("updateUnicodeStream");
        UPDATE_METHODS.add("updateCharacterStream");
        UPDATE_METHODS.add("updateBinaryStream");
        UPDATE_METHODS.add("updateBlob");
        UPDATE_METHODS.add("updateClob");
        UPDATE_METHODS.add("updateArray");
        UPDATE_METHODS.add("updateUrl");
        UPDATE_METHODS.add("updateRef");
        UPDATE_METHODS.add("updateNString");
        UPDATE_METHODS.add("updateNClob");
        UPDATE_METHODS.add("updateNCharacterStream");
        UPDATE_METHODS.add("updateSQLXML");
        UPDATE_METHODS.add("updateNull");
        UPDATE_METHODS.add("updateObject");

        MOVE_METHODS.add("next");
        MOVE_METHODS.add("previous");
        MOVE_METHODS.add("first");
        MOVE_METHODS.add("last");
        MOVE_METHODS.add("absolute");
        MOVE_METHODS.add("relative");
        MOVE_METHODS.add("afterLast");
        MOVE_METHODS.add("beforeFirst");
        MOVE_METHODS.add("moveToCurrentRow");
        MOVE_METHODS.add("moveToInsertRow");
        MOVE_METHODS.add("close");

        EXECUTE_METHODS.add("execute");
        EXECUTE_METHODS.add("executeUpdate");
        EXECUTE_METHODS.add("executeQuery");
        EXECUTE_METHODS.add("addBatch");

        RESULTSET_METHODS.add("executeQuery");
        RESULTSET_METHODS.add("getResultSet");
    }

    public JdbcLogSupport() {
        log.debug("#{} {}.Create()", id, getClass().getSimpleName());
    }

    protected void addParam(Object param, Object value) {
        paramNameList.add(param);
        paramValueList.add(value);
        paramTypeList.add((value == null) ? "null" : value.getClass().getName());
    }

    protected boolean isParamNotEmpty() {
        return !paramNameList.isEmpty();
    }

    protected String getParamNameList() {
        return paramNameList.toString();
    }

    protected String getParamValueList() {
        return paramValueList.toString();
    }

    protected String getParamTypeList() {
        return paramTypeList.toString();
    }

    protected void resetParamsInfo() {
        paramNameList.clear();
        paramValueList.clear();
        paramTypeList.clear();
    }

    protected String formatSQL(String original) {
        return original.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ');
    }

    /**
     * Examines a Throwable object and gets it's root cause
     * 
     * @param t
     *            - the exception to examine
     * @return The root cause
     */
    protected Throwable unwrapThrowable(Throwable t) {
        Throwable e = t;
        while (true) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) t).getTargetException();
            } else if (t instanceof UndeclaredThrowableException) {
                e = ((UndeclaredThrowableException) t).getUndeclaredThrowable();
            } else {
                return e;
            }
        }
    }

}
