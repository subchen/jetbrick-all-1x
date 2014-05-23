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
package jetbrick.dao.orm;

import java.sql.*;
import java.util.*;
import java.util.Date;
import javax.sql.DataSource;
import jetbrick.dao.DbException;
import jetbrick.dao.TransactionException;
import jetbrick.dao.dialect.SqlDialect;
import jetbrick.dao.orm.handlers.*;
import jetbrick.dao.orm.mappers.*;
import jetbrick.dao.orm.tx.*;
import jetbrick.dao.orm.utils.DbUtils;
import jetbrick.dao.orm.utils.PreparedStatementCreator;
import jetbrick.lang.Validate;

/**
 * 数据库操作。单例使用
 */
@SuppressWarnings("unchecked")
public class DbHelper {
    private static final boolean ALLOW_NESTED_TRANSACTION = System.getProperty("jetbrick.orm.transaction.nested.disabled") == null;

    // 当前线程(事务)
    private final ThreadLocal<JdbcTransaction> transationHandler = new ThreadLocal<JdbcTransaction>();
    private final DataSource dataSource;
    private final SqlDialect dialect;

    public DbHelper(DataSource dataSource) {
        this.dataSource = dataSource;
        this.dialect = doGetDialet();
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * 启动一个事务(默认支持子事务)
     */
    public Transaction transaction() {
        if (transationHandler.get() != null) {
            if (ALLOW_NESTED_TRANSACTION) {
                return new JdbcNestedTransaction(transationHandler.get().getConnection());
            }
            throw new TransactionException("Can't begin a nested transaction.");
        }
        try {
            JdbcTransaction tx = new JdbcTransaction(dataSource.getConnection(), transationHandler);
            transationHandler.set(tx);
            return tx;
        } catch (SQLException e) {
            throw new TransactionException(e);
        }
    }

    /**
     * 获取一个当前线程的连接(事务中)，如果没有，则新建一个。
     */
    private Connection getConnection() {
        JdbcTransaction tx = transationHandler.get();
        try {
            if (tx == null) {
                return dataSource.getConnection();
            } else {
                return tx.getConnection();
            }
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    /**
     * 释放一个连接，如果 Connection 不在事务中，则关闭它，否则不处理。
     */
    private void closeConnection(Connection conn) {
        if (transationHandler.get() == null) {
            // not in transaction
            DbUtils.closeQuietly(conn);
        }
    }

    public <T> List<T> queryAsList(RowMapper<T> rowMapper, String sql, Object... parameters) {
        Validate.notNull(rowMapper, "rowMapper is null.");

        ResultSetHandler<List<T>> rsh = new RowListHandler<T>(rowMapper);
        return query(rsh, sql, parameters);
    }

    public <T> List<T> queryAsList(Class<T> beanClass, String sql, Object... parameters) {
        Validate.notNull(beanClass, "beanClass is null.");

        RowMapper<T> rowMapper = getRowMapper(beanClass);
        return queryAsList(rowMapper, sql, parameters);
    }

    public <T> T queryAsObject(RowMapper<T> rowMapper, String sql, Object... parameters) {
        ResultSetHandler<T> rsh = new SingleRowHandler<T>(rowMapper);
        return query(rsh, sql, parameters);
    }

    public <T> T queryAsObject(Class<T> beanClass, String sql, Object... parameters) {
        Validate.notNull(beanClass, "beanClass is null.");

        RowMapper<T> rowMapper = getRowMapper(beanClass);
        return queryAsObject(rowMapper, sql, parameters);
    }

    public Integer queryAsInt(String sql, Object... parameters) {
        return queryAsObject(Integer.class, sql, parameters);
    }

    public Long queryAsLong(String sql, Object... parameters) {
        return queryAsObject(Long.class, sql, parameters);
    }

    public String queryAsString(String sql, Object... parameters) {
        return queryAsObject(String.class, sql, parameters);
    }

    public Boolean queryAsBoolean(String sql, Object... parameters) {
        return queryAsObject(Boolean.class, sql, parameters);
    }

    public Date queryAsDate(String sql, Object... parameters) {
        return queryAsObject(Date.class, sql, parameters);
    }

    public Map<String, Object> queryAsMap(String sql, Object... parameters) {
        return queryAsObject(Map.class, sql, parameters);
    }

    public <T> T[] queryAsArray(Class<T> arrayComponentClass, String sql, Object... parameters) {
        try {
            Class<T[]> clazz = (Class<T[]>) Class.forName("[" + arrayComponentClass.getName());
            return queryAsObject(clazz, sql, parameters);
        } catch (ClassNotFoundException e) {
            throw new DbException(e);
        }
    }

    public <T> Pagelist<T> queryAsPagelist(PageInfo pageInfo, Class<T> beanClass, String sql, Object... parameters) {
        Validate.notNull(beanClass, "beanClass is null.");

        RowMapper<T> rowMapper = getRowMapper(beanClass);
        return queryAsPagelist(pageInfo, rowMapper, sql, parameters);
    }

    public <T> Pagelist<T> queryAsPagelist(PageInfo pageInfo, RowMapper<T> rowMapper, String sql, Object... parameters) {
        Validate.notNull(pageInfo, "pageInfo is null.");
        Validate.notNull(rowMapper, "rowMapper is null.");

        PagelistImpl<T> pagelist = new PagelistImpl<T>(pageInfo);
        if (pageInfo.getTotalCount() < 0) {
            String count_sql = DbUtils.get_sql_select_count(sql);
            int count = queryAsInt(count_sql, parameters);
            pagelist.setTotalCount(count);
        }

        List<T> items = Collections.emptyList();
        if (pagelist.getTotalCount() > 0) {
            String page_sql = dialect.sql_pagelist(sql, pagelist.getFirstResult(), pagelist.getPageSize());
            PagelistHandler<T> rsh = new PagelistHandler<T>(rowMapper);
            if (page_sql == null) {
                // 如果不支持分页，那么使用原始的分页方法 ResultSet.absolute(first)
                rsh.setFirstResult(pagelist.getFirstResult());
            } else {
                // 使用数据库自身的分页SQL语句，将直接返回某一个
                rsh.setFirstResult(0);
                sql = page_sql;
            }
            rsh.setMaxResults(pagelist.getPageSize());
            items = query(rsh, sql, parameters);
        }
        pagelist.setItems(items);

        return pagelist;
    }

    public <T> T query(ResultSetHandler<T> rsh, String sql, Object... parameters) {
        Validate.notNull(rsh, "rsh is null.");
        Validate.notNull(sql, "sql is null.");

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        T result = null;

        try {
            conn = getConnection();
            ps = PreparedStatementCreator.createPreparedStatement(conn, sql, parameters);
            rs = ps.executeQuery();
            result = rsh.handle(rs);
        } catch (SQLException e) {
            throw new DbException(e).set("sql", sql).set("parameters", parameters);
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(ps);
            closeConnection(conn);
        }

        return result;
    }

    public int execute(String sql, Object... parameters) {
        Validate.notNull(sql, "sql is null.");

        Connection conn = null;
        PreparedStatement ps = null;
        int rows = 0;

        try {
            conn = getConnection();
            ps = PreparedStatementCreator.createPreparedStatement(conn, sql, parameters);
            rows = ps.executeUpdate();
        } catch (SQLException e) {
            throw new DbException(e).set("sql", sql).set("parameters", parameters);
        } finally {
            DbUtils.closeQuietly(ps);
            closeConnection(conn);
        }

        return rows;
    }

    public int[] executeBatch(String sql, List<Object[]> parameters) {
        Validate.notNull(sql, "sql is null.");

        Connection conn = null;
        PreparedStatement ps = null;
        int[] rows;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            for (Object[] parameter : parameters) {
                for (int i = 0; i < parameter.length; i++) {
                    ps.setObject(i + 1, parameter[i]);
                }
                ps.addBatch();
            }
            rows = ps.executeBatch();
        } catch (SQLException e) {
            throw new DbException(e).set("sql", sql).set("parameters", parameters);
        } finally {
            DbUtils.closeQuietly(ps);
            closeConnection(conn);
        }

        return rows;
    }

    public void execute(ConnectionCallback callback) {
        Connection conn = null;
        try {
            conn = getConnection();
            callback.execute(conn);
        } catch (SQLException e) {
            throw new DbException(e);
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * 判断表是否已经存在
     */
    public boolean tableExist(String name) {
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            DatabaseMetaData metaData = conn.getMetaData();
            rs = metaData.getTables(null, null, name.toUpperCase(), new String[] { "TABLE" });
            return rs.next();
        } catch (SQLException e) {
            throw new DbException(e);
        } finally {
            DbUtils.closeQuietly(rs);
            closeConnection(conn);
        }
    }

    public SqlDialect getDialect() {
        return dialect;
    }

    private SqlDialect doGetDialet() {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            String name = conn.getMetaData().getDatabaseProductName();
            return SqlDialect.getDialect(name);
        } catch (SQLException e) {
            throw new DbException(e);
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    public <T> RowMapper<T> getRowMapper(Class<T> beanClass) {
        RowMapper<T> rowMapper;
        if (beanClass.isArray()) {
            rowMapper = (RowMapper<T>) new ArrayRowMapper();
        } else if (beanClass.getName().equals("java.util.Map")) {
            rowMapper = (RowMapper<T>) new MapRowMapper();
        } else if (beanClass.getName().startsWith("java.")) {
            rowMapper = new SingleColumnRowMapper<T>(beanClass);
        } else {
            rowMapper = new BeanRowMapper<T>(beanClass);
        }
        return rowMapper;
    }
}
