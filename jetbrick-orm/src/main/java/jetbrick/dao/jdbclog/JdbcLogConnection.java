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
import java.sql.*;

/**
 * Connection Wrapper to add logging
 */
public final class JdbcLogConnection extends JdbcLogSupport implements InvocationHandler {
    private final Connection connection;

    /**
     * Creates a logging version of a connection
     *
     * @param conn - the original connection
     * @return - the connection with logging
     */
    public static Connection getInstance(Connection conn) {
        InvocationHandler handler = new JdbcLogConnection(conn);
        ClassLoader cl = Connection.class.getClassLoader();
        return (Connection) Proxy.newProxyInstance(cl, new Class[] { Connection.class }, handler);
    }

    private JdbcLogConnection(Connection conn) {
        this.connection = conn;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        try {
            String methodName = method.getName();
            if ("prepareStatement".equals(methodName)) {
                PreparedStatement stmt = (PreparedStatement) method.invoke(connection, params);
                return JdbcLogPreparedStatement.getInstance(stmt, (String) params[0]);
            } else if ("prepareCall".equals(methodName)) {
                CallableStatement stmt = (CallableStatement) method.invoke(connection, params);
                return JdbcLogCallableStatement.getInstance(stmt, (String) params[0]);
            } else if ("createStatement".equals(methodName)) {
                Statement stmt = (Statement) method.invoke(connection, params);
                return JdbcLogStatement.getInstance(stmt);
            } else {
                return method.invoke(connection, params);
            }
        } catch (Throwable t) {
            log.error("#{} <ERROR> in {}", id, toString(method, params));
            throw unwrapThrowable(t);
        }
    }
}
