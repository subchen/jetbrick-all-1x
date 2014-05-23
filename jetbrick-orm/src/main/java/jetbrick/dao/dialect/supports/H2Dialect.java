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
package jetbrick.dao.dialect.supports;

import jetbrick.dao.dialect.*;
import jetbrick.lang.StringUtils;

public class H2Dialect extends SqlDialect {
    public static final String NAME = "H2";

    @Override
    protected String getQuotedIdentifier(String name) {
        return "\"" + StringUtils.replace(name, "\"", "\"\"") + "\"";
    }

    @Override
    public String sql_table_drop(String table) {
        return String.format("drop table if exists %s;", getIdentifier(table));
    }

    @Override
    public String sql_table_rename(String oldName, String newName) {
        return String.format("alter table  %s rename to %s;", getIdentifier(oldName), getIdentifier(newName));
    }

    @Override
    public String sql_column_add(String table, String column_definition, String column_position) {
        String sql = String.format("alter table %s add column %s", getIdentifier(table), column_definition);
        if (supportsColumnPosition() && column_position != null) {
            sql = sql + " " + column_position;
        }
        return sql;
    }

    @Override
    public String sql_column_modify(String table, String column_definition, String column_position) {
        String sql = String.format("alter table %s alter column %s", getIdentifier(table), column_definition);
        if (supportsColumnPosition() && column_position != null) {
            sql = sql + " " + column_position;
        }
        return sql;
    }

    @Override
    public String sql_column_drop(String table, String column) {
        return String.format("alter table %s drop column %s;", getIdentifier(table), getIdentifier(column));
    }

    @Override
    public String sql_pagelist(String sql, int offset, int limit) {
        sql = sql + " limit " + limit;
        if (offset > 0) {
            sql = sql + " offset " + offset;
        }
        return sql;
    }

    @Override
    public boolean supportsColumnPosition() {
        return true;
    }

    @Override
    public boolean supportsSequences() {
        return true;
    }

    @Override
    public String getHibernateDialect() {
        return "org.hibernate.dialect.H2Dialect";
    }

    @Override
    public String asSqlType(String type, Integer length, Integer scale) {
        if (SubStyleType.TEXT.equals(type)) {
            return new SqlType("varchar", Integer.MAX_VALUE, null).toString();
        } else if (SubStyleType.BOOLEAN.equals(type)) {
            return "boolean";
        } else if (SubStyleType.INPUTSTREAM.equals(type)) {
            return "binary";
        }
        return super.asSqlType(type, length, scale);
    }

    @Override
    protected void initializeReservedWords() {
        reservedWords.add("CROSS");
        reservedWords.add("CURRENT_DATE");
        reservedWords.add("CURRENT_TIME");
        reservedWords.add("CURRENT_TIMESTAMP");
        reservedWords.add("DISTINCT");
        reservedWords.add("EXCEPT");
        reservedWords.add("EXISTS");
        reservedWords.add("FALSE");
        reservedWords.add("FOR");
        reservedWords.add("FROM");
        reservedWords.add("FULL");
        reservedWords.add("GROUP");
        reservedWords.add("HAVING");
        reservedWords.add("INNER");
        reservedWords.add("INTERSECT");
        reservedWords.add("IS");
        reservedWords.add("JOIN");
        reservedWords.add("LIKE");
        reservedWords.add("LIMIT");
        reservedWords.add("MINUS");
        reservedWords.add("NATURAL");
        reservedWords.add("NOT");
        reservedWords.add("NULL");
        reservedWords.add("ON");
        reservedWords.add("ORDER");
        reservedWords.add("PRIMARY");
        reservedWords.add("ROWNUM");
        reservedWords.add("SELECT");
        reservedWords.add("SYSDATE");
        reservedWords.add("SYSTIME");
        reservedWords.add("SYSTIMESTAMP");
        reservedWords.add("TODAY");
        reservedWords.add("TRUE");
        reservedWords.add("UNION");
        reservedWords.add("UNIQUE");
        reservedWords.add("WHERE");
    }

}
