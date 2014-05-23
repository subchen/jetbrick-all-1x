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

public class OracleDialect extends SqlDialect {
    public static final String NAME = "Oracle";

    @Override
    protected String getQuotedIdentifier(String name) {
        return "\"" + name + "\"";
    }

    @Override
    public String sql_table_drop(String table) {
        return String.format("drop table %s;", getIdentifier(table));
    }

    @Override
    public String sql_table_rename(String oldName, String newName) {
        oldName = getIdentifier(oldName);
        newName = getIdentifier(newName);
        return "alter table  " + oldName + " rename to " + newName + ";";
    }

    @Override
    public String sql_column_add(String table, String column_definition, String column_position) {
        return String.format("alter table %s add column %s;", getIdentifier(table), column_definition);
    }

    @Override
    public String sql_column_modify(String table, String column_definition, String column_positions) {
        return String.format("alter table %s alter column %s;", getIdentifier(table), column_definition);
    }

    @Override
    public String sql_column_drop(String table, String column) {
        return String.format("alter table %s drop column %s;", getIdentifier(table), getIdentifier(column));
    }

    @Override
    public String sql_pagelist(String sql, int offset, int limit) {
        //@formatter:off
        sql = "select * from ("
            + "  select t.*, ROWNUM row from ("
            +      sql
            + "  ) t where ROWNUM <= " + (offset + limit) + ")";
        //@formatter:on
        if (offset > 0) {
            sql = sql + " where row > " + offset;
        }
        return sql;
    }

    @Override
    public String getHibernateDialect() {
        return "org.hibernate.dialect.OracleDialect";
    }

    @Override
    public String asSqlType(String type, Integer length, Integer scale) {
        if (SubStyleType.CHAR.equals(type)) {
            return new SqlType("nchar", length, null).toString();
        } else if (SubStyleType.VARCHAR.equals(type)) {
            return new SqlType("nvarchar2", length, null).toString();
        } else if (SubStyleType.TEXT.equals(type)) {
            return "nclob";
        } else if (SubStyleType.BOOLEAN.equals(type)) {
            return "number(1)";
        } else if (SubStyleType.INT.equals(type)) {
            return "number(10)";
        } else if (SubStyleType.LONG.equals(type)) {
            return "number(20, 0)";
        } else if (SubStyleType.BIGINT.equals(type)) {
            return "number(38, 0)";
        } else if (SubStyleType.DOUBLE.equals(type)) {
            return "number(38,10)";
        } else if (SubStyleType.DECIMAL.equals(type)) {
            return new SqlType("number", length, scale).toString();
        } else if (SubStyleType.DATETIME.equals(type)) {
            return "date";
        } else if (SubStyleType.TIMESTAMP.equals(type)) {
            return "date";
        } else if (SubStyleType.DATE.equals(type)) {
            return "date";
        } else if (SubStyleType.TIME.equals(type)) {
            return "date";
        } else if (SubStyleType.CLOB.equals(type)) {
            return "nclob";
        } else if (SubStyleType.INPUTSTREAM.equals(type)) {
            return "blob";
        }
        return super.asSqlType(type, length, scale);
    }

    @Override
    protected void initializeReservedWords() {
        reservedWords.add("ACCESS");
        reservedWords.add("ADD");
        reservedWords.add("ALL");
        reservedWords.add("ALTER");
        reservedWords.add("AND");
        reservedWords.add("ANY");
        reservedWords.add("AS");
        reservedWords.add("ASC");
        reservedWords.add("AUDIT");
        reservedWords.add("BETWEEN");
        reservedWords.add("BY");
        reservedWords.add("CHAR");
        reservedWords.add("CHECK");
        reservedWords.add("CLUSTER");
        reservedWords.add("COLUMN");
        reservedWords.add("COMMENT");
        reservedWords.add("COMPRESS");
        reservedWords.add("CONNECT");
        reservedWords.add("CREATE");
        reservedWords.add("CURRENT");
        reservedWords.add("DATE");
        reservedWords.add("DECIMAL");
        reservedWords.add("DEFAULT");
        reservedWords.add("DELETE");
        reservedWords.add("DESC");
        reservedWords.add("DISTINCT");
        reservedWords.add("DROP");
        reservedWords.add("ELSE");
        reservedWords.add("EXCLUSIVE");
        reservedWords.add("EXISTS");
        reservedWords.add("FILE");
        reservedWords.add("FLOAT");
        reservedWords.add("FOR");
        reservedWords.add("FROM");
        reservedWords.add("GRANT");
        reservedWords.add("GROUP");
        reservedWords.add("HAVING");
        reservedWords.add("IDENTIFIED");
        reservedWords.add("IMMEDIATE");
        reservedWords.add("IN");
        reservedWords.add("INCREMENT");
        reservedWords.add("INDEX");
        reservedWords.add("INITIAL");
        reservedWords.add("INSERT");
        reservedWords.add("INTEGER");
        reservedWords.add("INTERSECT");
        reservedWords.add("INTO");
        reservedWords.add("IS");
        reservedWords.add("LEVEL");
        reservedWords.add("LIKE");
        reservedWords.add("LOCK");
        reservedWords.add("LONG");
        reservedWords.add("MAXEXTENTS");
        reservedWords.add("MINUS");
        reservedWords.add("MLSLABEL");
        reservedWords.add("MODE");
        reservedWords.add("MODIFY");
        reservedWords.add("NOAUDIT");
        reservedWords.add("NOCOMPRESS");
        reservedWords.add("NOT");
        reservedWords.add("NOWAIT");
        reservedWords.add("NULL");
        reservedWords.add("NUMBER");
        reservedWords.add("OF");
        reservedWords.add("OFFLINE");
        reservedWords.add("ON");
        reservedWords.add("ONLINE");
        reservedWords.add("OPTION");
        reservedWords.add("OR");
        reservedWords.add("ORDER");
        reservedWords.add("PCTFREE");
        reservedWords.add("PRIOR");
        reservedWords.add("PRIVILEGES");
        reservedWords.add("PUBLIC");
        reservedWords.add("RAW");
        reservedWords.add("RENAME");
        reservedWords.add("RESOURCE");
        reservedWords.add("REVOKE");
        reservedWords.add("ROW");
        reservedWords.add("ROWID");
        reservedWords.add("ROWNUM");
        reservedWords.add("ROWS");
        reservedWords.add("SELECT");
        reservedWords.add("SESSION");
        reservedWords.add("SET");
        reservedWords.add("SHARE");
        reservedWords.add("SIZE");
        reservedWords.add("SMALLINT");
        reservedWords.add("START");
        reservedWords.add("SUCCESSFUL");
        reservedWords.add("SYNONYM");
        reservedWords.add("SYSDATE");
        reservedWords.add("TABLE");
        reservedWords.add("THEN");
        reservedWords.add("TO");
        reservedWords.add("TRIGGER");
        reservedWords.add("UID");
        reservedWords.add("UNION");
        reservedWords.add("UNIQUE");
        reservedWords.add("UPDATE");
        reservedWords.add("USER");
        reservedWords.add("VALIDATE");
        reservedWords.add("VALUES");
        reservedWords.add("VARCHAR");
        reservedWords.add("VARCHAR2");
        reservedWords.add("VIEW");
        reservedWords.add("WHENEVER");
        reservedWords.add("WHERE");
        reservedWords.add("WITH");
    }

}
