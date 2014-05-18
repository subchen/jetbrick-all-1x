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

import java.sql.*;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import jetbrick.dao.dialect.SqlDialect;
import jetbrick.lang.ExceptionUtils;

public final class DbUtils {
    public static void closeQuietly(Connection conn) {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
        }
    }

    public static void closeQuietly(Statement stmt) {
        try {
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
        }
    }

    public static void closeQuietly(ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
        }
    }

    public static boolean doGetTableExist(Connection conn, String tabelName) {
        ResultSet rs = null;
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            rs = metaData.getTables(null, null, tabelName.toUpperCase(), new String[] { "TABLE" });
            return rs.next();
        } catch (SQLException e) {
            throw ExceptionUtils.unchecked(e);
        } finally {
            DbUtils.closeQuietly(rs);
        }
    }

    public static SqlDialect doGetDialect(DataSource dataSource) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            return DbUtils.doGetDialect(conn);
        } catch (SQLException e) {
            throw ExceptionUtils.unchecked(e);
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    public static SqlDialect doGetDialect(Connection conn) {
        try {
            String name = conn.getMetaData().getDatabaseProductName();
            return SqlDialect.getDialect(name);
        } catch (SQLException e) {
            throw ExceptionUtils.unchecked(e);
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    // 从一个标准的select语句中，生成一个 select count(*) 语句
    public static String get_sql_select_count(String sql) {
        String count_sql = sql.replaceAll("\\s+", " ");
        int pos = count_sql.toLowerCase().indexOf(" from ");
        count_sql = count_sql.substring(pos);

        pos = count_sql.toLowerCase().lastIndexOf(" order by ");
        int lastpos = count_sql.toLowerCase().lastIndexOf(")");
        if (pos != -1 && pos > lastpos) {
            count_sql = count_sql.substring(0, pos);
        }

        String regex = "(left|right|inner) join (fetch )?\\w+(\\.\\w+)*";
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        count_sql = p.matcher(count_sql).replaceAll("");

        count_sql = "select count(*) " + count_sql;
        return count_sql;
    }
}
