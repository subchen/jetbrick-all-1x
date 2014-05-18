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
package jetbrick.dao.id;

import java.sql.*;
import javax.sql.DataSource;

public class JdbcSequenceIdProvider implements SequenceIdProvider {

    private static final String TABLE_NAME = "_SEQUANCE_";
    private final DataSource dataSource;

    public JdbcSequenceIdProvider(DataSource dataSource) {
        this.dataSource = dataSource;

        confirmTableExists();
    }

    @Override
    public SequenceId create(String name) {
        return new SequenceId(this, name, 1);
    }

    @Override
    public SequenceId create(String name, int begin) {
        return new SequenceId(this, name, begin);
    }

    private void confirmTableExists() {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();

            ResultSet rs = conn.getMetaData().getTables(null, null, TABLE_NAME, null);
            boolean found = rs.next();
            rs.close();

            if (!found) {
                Statement stmt = conn.createStatement();
                String sql = "create table " + TABLE_NAME + " (name varchar(50) not null, next_val long not null, primary key(name))";
                stmt.execute(sql);
                stmt.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(conn);
        }
    }

    private void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public int load(String name) {
        int value = SequenceId.NOT_FOUND;
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            String sql = "select next_val from " + TABLE_NAME + " where name=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                value = rs.getInt(1);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(conn);
        }
        return value;
    }

    @Override
    public void store(String name, int value) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            String sql = "update " + TABLE_NAME + " set next_val=? where name=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, value);
            ps.setString(2, name);
            int updated = ps.executeUpdate();
            ps.close();

            if (updated == 0) {
                sql = "insert into " + TABLE_NAME + " (name, next_val) values (?,?)";
                ps = conn.prepareStatement(sql);
                ps.setString(1, name);
                ps.setInt(2, value);
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(conn);
        }
    }
}
