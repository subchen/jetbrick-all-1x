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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PreparedStatement Wrapper to add logging
 */
public final class JdbcLogPreparedStatement extends JdbcLogSupport implements InvocationHandler {
    private static final Logger log = LoggerFactory.getLogger(JdbcLogPreparedStatement.class);
    private final PreparedStatement statement;
    private final String sql;

    /**
     * Creates a logging version of a PreparedStatement
     *
     * @param stmt
     *            - the statement
     * @param sql
     *            - the sql statement
     * @return - the proxy
     */
    public static PreparedStatement getInstance(PreparedStatement stmt, String sql) {
        InvocationHandler handler = new JdbcLogPreparedStatement(stmt, sql);
        ClassLoader cl = PreparedStatement.class.getClassLoader();
        return (PreparedStatement) Proxy.newProxyInstance(cl, new Class[] { PreparedStatement.class }, handler);
    }

    private JdbcLogPreparedStatement(PreparedStatement stmt, String sql) {
        this.statement = stmt;
        this.sql = sql;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        try {
            String methodName = method.getName();
            if (EXECUTE_METHODS.contains(methodName)) {
                if (log.isDebugEnabled()) {
                    log.debug("#{} PreparedStatement.{}(): {}", id, methodName, formatSql(sql));
                    log.debug("#{} Parameters: {}", id, getParamValueList());
                    log.debug("#{} Types: {}", id, getParamTypeList());
                }
                resetParamsInfo();
            } else if (RESULTSET_METHODS.contains(methodName)) {
                Object value = method.invoke(statement, params);
                if (value != null && value instanceof ResultSet) {
                    value = JdbcLogResultSet.getInstance((ResultSet) value);
                }
                return value;
            } else if (SET_METHODS.contains(methodName)) {
                if ("setNull".equals(methodName)) {
                    addParam(params[0], null);
                } else {
                    addParam(params[0], params[1]);
                }
            }

            return method.invoke(statement, params);
        } catch (Exception e) {
            log.error("#{} <ERROR> in {}", id, toString(method, params));
            throw unwrapThrowable(e);
        }
    }
}
