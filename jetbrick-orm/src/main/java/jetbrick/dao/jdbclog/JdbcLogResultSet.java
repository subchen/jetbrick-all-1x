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

import java.lang.reflect.*;
import java.sql.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ResultSet Wrapper to add logging
 */
public class JdbcLogResultSet extends JdbcLogSupport implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(JdbcLogResultSet.class);
    private ResultSet rs;

    private JdbcLogResultSet(ResultSet rs) {
        this.rs = rs;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        try {
            Object value = method.invoke(rs, params);
            if (GET_METHODS.contains(method.getName())) {
                String columnName;
                if (params[0] instanceof String) {
                    columnName = (String) params[0];
                } else {
                    Integer index = (Integer) params[0];
                    columnName = rs.getMetaData().getColumnName(index);
                }
                if (rs.wasNull()) {
                    value = null;
                }
                addParam(columnName, value);
            } else if (MOVE_METHODS.contains(method.getName())) {
                if (isParamNotEmpty() && log.isDebugEnabled()) {
                    log.debug("#{} ResultSet.Get(): {}", id, getParamNameList());
                    log.debug("#{} Parameters: {}", id, getParamValueList());
                    log.debug("#{} Types: {}", id, getParamTypeList());
                }
                resetParamsInfo();
            } else if (UPDATE_METHODS.contains(method.getName())) {
                String columnName;
                if (params[0] instanceof String) {
                    columnName = (String) params[0];
                } else {
                    Integer index = (Integer) params[0];
                    columnName = rs.getMetaData().getColumnName(index);
                }
                addParam(columnName, value);
            } else if ("updateRow".equals(method.getName()) || "cancelRowUpdates".equals(method.getName())) {
                if (log.isDebugEnabled()) {
                    log.debug("#{} ResultSet.{}(): {}", id, method.getName(), getParamNameList());
                    log.debug("#{} Parameters: {}", id, getParamValueList());
                    log.debug("#{} Types: {}", id, getParamTypeList());
                }
                resetParamsInfo();
            } else if ("insertRow".equals(method.getName())) {
                log.debug("#{} ResultSet.insertRow()", id);
                resetParamsInfo();
            }
            return value;
        } catch (Throwable t) {
            throw unwrapThrowable(t);
        }
    }

    /**
     * Creates a logging version of a ResultSet
     * 
     * @param rs
     *            - the ResultSet to proxy
     * @return - the ResultSet with logging
     */
    public static ResultSet getInstance(ResultSet rs) {
        if (rs instanceof JdbcLogResultSet) {
            return rs;
        } else {
            InvocationHandler handler = new JdbcLogResultSet(rs);
            ClassLoader cl = ResultSet.class.getClassLoader();
            return (ResultSet) Proxy.newProxyInstance(cl, new Class[] { ResultSet.class }, handler);
        }
    }

}
