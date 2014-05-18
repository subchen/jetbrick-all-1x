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
package jetbrick.dao.orm.tx;

import java.sql.Connection;
import java.sql.SQLException;
import jetbrick.dao.TransactionException;
import jetbrick.dao.orm.utils.DbUtils;

/**
 * Jdbc 事务对象
 */
public class JdbcTransaction implements Transaction {
    private final Connection conn;
    private final ThreadLocal<JdbcTransaction> transationHandler;

    public JdbcTransaction(Connection conn, ThreadLocal<JdbcTransaction> transationHandler) {
        this.conn = conn;
        this.transationHandler = transationHandler;

        try {
            if (conn.getAutoCommit()) {
                conn.setAutoCommit(false);
            }
        } catch (SQLException e) {
            throw new TransactionException(e);
        }
    }

    public Connection getConnection() {
        return conn;
    }

    /**
     * 提交一个事务
     */
    @Override
    public void commit() {
        try {
            if (conn.isClosed()) {
                throw new TransactionException("the connection is closed in transaction.");
            }
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new TransactionException(e);
        }
    }

    /**
     * 回滚一个事务
     */
    @Override
    public void rollback() {
        try {
            if (conn.isClosed()) {
                throw new TransactionException("the connection is closed in transaction.");
            }
            conn.rollback();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new TransactionException(e);
        }
    }

    /**
     * 结束一个事务
     */
    @Override
    public void close() {
        try {
            if (conn.isClosed()) {
                throw new TransactionException("the connection is closed in transaction.");
            }
            DbUtils.closeQuietly(conn);
        } catch (SQLException e) {
            throw new TransactionException(e);
        } finally {
            transationHandler.set(null);
        }
    }

}
