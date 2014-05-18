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
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Statement Wrapper to add logging
 */
public class JdbcLogStatement extends JdbcLogSupport implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(JdbcLogStatement.class);
    private Statement statement;

    private JdbcLogStatement(Statement stmt) {
        this.statement = stmt;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        try {
            Object value = method.invoke(statement, params);
            if (EXECUTE_METHODS.contains(method.getName())) {
                if (log.isDebugEnabled()) {
                    String sql = formatSQL((String) params[0]);
                    log.debug("#{} Statement.{}(): {}", id, method.getName(), sql);
                }
            }
            if (RESULTSET_METHODS.contains(method.getName())) {
                if (value != null && value instanceof ResultSet) {
                    value = JdbcLogResultSet.getInstance((ResultSet) value);
                }
            }
            return value;
        } catch (Throwable t) {
            throw unwrapThrowable(t);
        }
    }

    /**
     * Creates a logging version of a Statement
     * 
     * @param stmt
     *            - the statement
     * @return - the proxy
     */
    public static Statement getInstance(Statement stmt) {
        if (stmt instanceof JdbcLogStatement) {
            return stmt;
        } else {
            InvocationHandler handler = new JdbcLogStatement(stmt);
            ClassLoader cl = Statement.class.getClassLoader();
            return (Statement) Proxy.newProxyInstance(cl, new Class[] { Statement.class }, handler);
        }
    }

}
