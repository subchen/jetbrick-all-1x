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

public class SqlServerDialect extends SqlDialect {
    public static final String NAME = "Microsoft SQL Server";

    @Override
    protected String getQuotedIdentifier(String name) {
        return "[" + name + "]";
    }

    @Override
    public String getSqlStatmentSeparator() {
        return "go" + "\n";
    }

    @Override
    public String sql_table_drop(String table) {
        String sql = "if exists (select * from dbo.sysobjects where id=object_id(N'%s') and objectproperty(id,N'IsUserTable')=1) drop table %s;";
        table = getIdentifier(table);
        return String.format(sql, table, table);
    }

    @Override
    public String sql_table_rename(String oldName, String newName) {
        oldName = getIdentifier(oldName);
        newName = getIdentifier(newName);
        return "alter table  " + oldName + " rename to " + newName + ";";
    }

    @Override
    public String sql_column_add(String table, String column_definition, String column_position) {
        return String.format("alter table %s add %s;", getIdentifier(table), column_definition);
    }

    @Override
    public String sql_column_modify(String table, String column_definition, String column_position) {
        return String.format("alter table %s alter column %s;", getIdentifier(table), column_definition);
    }

    @Override
    public String sql_column_drop(String table, String column) {
        return String.format("alter table %s drop column %s;", getIdentifier(table), getIdentifier(column));
    }

    @Override
    public String sql_pagelist(String sql, int offset, int limit) {
        if (offset == 0) {
            sql = "select top " + limit + " * from (" + sql + ") as temp";
        } else {
            sql = sql.replaceAll("\\s+", " ");
            // 从原始 sql 中获取 order by 子句
            int orderby_pos = sql.toLowerCase().lastIndexOf(" order by ");
            String sorts = null;
            if (orderby_pos > 0) {
                sorts = sql.substring(orderby_pos);
                if (sorts.indexOf(")") > 0) {
                    sorts = null; // skip the nested order by
                }
            }
            if (sorts == null) {
                //sorts = "order by id";
                return null;
            }
            //@formatter:off
            sql = "select * from ("
                + "  select top " + (offset + limit) + " row_number() over(" + sorts + ") as row, * from (" + sql + ")"
                + ") as temp where row > " + offset;
            //@formatter:on
        }
        return sql;
    }

    @Override
    public String getHibernateDialect() {
        return "org.hibernate.dialect.SQLServerDialect";
    }

    @Override
    public String asSqlType(String type, Integer length, Integer scale) {
        if (SubStyleType.CHAR.equals(type)) {
            return new SqlType("nchar", length, null).toString();
        } else if (SubStyleType.VARCHAR.equals(type)) {
            return new SqlType("nvarchar", length, null).toString();
        } else if (SubStyleType.TEXT.equals(type)) {
            return "ntext";
        } else if (SubStyleType.LONG.equals(type)) {
            return "bigint";
        } else if (SubStyleType.BIGINT.equals(type)) {
            return "decimal(38, 0)";
        } else if (SubStyleType.DOUBLE.equals(type)) {
            return "float";
        } else if (SubStyleType.DATETIME.equals(type)) {
            return "datetime";
        } else if (SubStyleType.TIMESTAMP.equals(type)) {
            return "datetime";
        } else if (SubStyleType.INPUTSTREAM.equals(type)) {
            return "image";
        }
        return super.asSqlType(type, length, scale);
    }

    @Override
    protected void initializeReservedWords() {
        reservedWords.add("ADD");
        reservedWords.add("ALL");
        reservedWords.add("ALTER");
        reservedWords.add("AND");
        reservedWords.add("ANY");
        reservedWords.add("AS");
        reservedWords.add("ASC");
        reservedWords.add("AUTHORIZATION");
        reservedWords.add("BACKUP");
        reservedWords.add("BEGIN");
        reservedWords.add("BETWEEN");
        reservedWords.add("BREAK");
        reservedWords.add("BROWSE");
        reservedWords.add("BULK");
        reservedWords.add("BY");
        reservedWords.add("CASCADE");
        reservedWords.add("CASE");
        reservedWords.add("CHECK");
        reservedWords.add("CHECKPOINT");
        reservedWords.add("CLOSE");
        reservedWords.add("CLUSTERED");
        reservedWords.add("COALESCE");
        reservedWords.add("COLLATE");
        reservedWords.add("COLUMN");
        reservedWords.add("COMMIT");
        reservedWords.add("COMPUTE");
        reservedWords.add("CONSTRAINT");
        reservedWords.add("CONTAINS");
        reservedWords.add("CONTAINSTABLE");
        reservedWords.add("CONTINUE");
        reservedWords.add("CONVERT");
        reservedWords.add("CREATE");
        reservedWords.add("CROSS");
        reservedWords.add("CURRENT");
        reservedWords.add("CURRENT_DATE");
        reservedWords.add("CURRENT_TIME");
        reservedWords.add("CURRENT_TIMESTAMP");
        reservedWords.add("CURRENT_USER");
        reservedWords.add("CURSOR");
        reservedWords.add("DATABASE");
        reservedWords.add("DBCC");
        reservedWords.add("DEALLOCATE");
        reservedWords.add("DECLARE");
        reservedWords.add("DEFAULT");
        reservedWords.add("DELETE");
        reservedWords.add("DENY");
        reservedWords.add("DESC");
        reservedWords.add("DISK");
        reservedWords.add("DISTINCT");
        reservedWords.add("DISTRIBUTED");
        reservedWords.add("DOUBLE");
        reservedWords.add("DROP");
        reservedWords.add("DUMMY");
        reservedWords.add("DUMP");
        reservedWords.add("ELSE");
        reservedWords.add("END");
        reservedWords.add("ERRLVL");
        reservedWords.add("ESCAPE");
        reservedWords.add("EXCEPT");
        reservedWords.add("EXEC");
        reservedWords.add("EXECUTE");
        reservedWords.add("EXISTS");
        reservedWords.add("EXIT");
        reservedWords.add("FETCH");
        reservedWords.add("FILE");
        reservedWords.add("FILLFACTOR");
        reservedWords.add("FOR");
        reservedWords.add("FOREIGN");
        reservedWords.add("FREETEXT");
        reservedWords.add("FREETEXTTABLE");
        reservedWords.add("FROM");
        reservedWords.add("FULL");
        reservedWords.add("FUNCTION");
        reservedWords.add("GOTO");
        reservedWords.add("GRANT");
        reservedWords.add("GROUP");
        reservedWords.add("HAVING");
        reservedWords.add("HOLDLOCK");
        reservedWords.add("IDENTITY");
        reservedWords.add("IDENTITYCOL");
        reservedWords.add("IDENTITY_INSERT");
        reservedWords.add("IF");
        reservedWords.add("IN");
        reservedWords.add("INDEX");
        reservedWords.add("INNER");
        reservedWords.add("INSERT");
        reservedWords.add("INTERSECT");
        reservedWords.add("INTO");
        reservedWords.add("IS");
        reservedWords.add("JOIN");
        reservedWords.add("KEY");
        reservedWords.add("KILL");
        reservedWords.add("LEFT");
        reservedWords.add("LIKE");
        reservedWords.add("LINENO");
        reservedWords.add("LOAD");
        reservedWords.add("NATIONAL");
        reservedWords.add("NOCHECK");
        reservedWords.add("NONCLUSTERED");
        reservedWords.add("NOT");
        reservedWords.add("NULL");
        reservedWords.add("NULLIF");
        reservedWords.add("OF");
        reservedWords.add("OFF");
        reservedWords.add("OFFSETS");
        reservedWords.add("ON");
        reservedWords.add("OPEN");
        reservedWords.add("OPENDATASOURCE");
        reservedWords.add("OPENQUERY");
        reservedWords.add("OPENROWSET");
        reservedWords.add("OPENXML");
        reservedWords.add("OPTION");
        reservedWords.add("OR");
        reservedWords.add("ORDER");
        reservedWords.add("OUTER");
        reservedWords.add("OVER");
        reservedWords.add("PERCENT");
        reservedWords.add("PLAN");
        reservedWords.add("PRECISION");
        reservedWords.add("PRIMARY");
        reservedWords.add("PRINT");
        reservedWords.add("PROC");
        reservedWords.add("PROCEDURE");
        reservedWords.add("PUBLIC");
        reservedWords.add("RAISERROR");
        reservedWords.add("READ");
        reservedWords.add("READTEXT");
        reservedWords.add("RECONFIGURE");
        reservedWords.add("REFERENCES");
        reservedWords.add("REPLICATION");
        reservedWords.add("RESTORE");
        reservedWords.add("RESTRICT");
        reservedWords.add("RETURN");
        reservedWords.add("REVOKE");
        reservedWords.add("RIGHT");
        reservedWords.add("ROLLBACK");
        reservedWords.add("ROWCOUNT");
        reservedWords.add("ROWGUIDCOL");
        reservedWords.add("RULE");
        reservedWords.add("SAVE");
        reservedWords.add("SCHEMA");
        reservedWords.add("SELECT");
        reservedWords.add("SESSION_USER");
        reservedWords.add("SET");
        reservedWords.add("SETUSER");
        reservedWords.add("SHUTDOWN");
        reservedWords.add("SOME");
        reservedWords.add("STATISTICS");
        reservedWords.add("SYSTEM_USER");
        reservedWords.add("TABLE");
        reservedWords.add("TEXTSIZE");
        reservedWords.add("THEN");
        reservedWords.add("TO");
        reservedWords.add("TOP");
        reservedWords.add("TRAN");
        reservedWords.add("TRANSACTION");
        reservedWords.add("TRIGGER");
        reservedWords.add("TRUNCATE");
        reservedWords.add("TSEQUAL");
        reservedWords.add("UNION");
        reservedWords.add("UNIQUE");
        reservedWords.add("UPDATE");
        reservedWords.add("UPDATETEXT");
        reservedWords.add("USE");
        reservedWords.add("USER");
        reservedWords.add("VALUES");
        reservedWords.add("VARYING");
        reservedWords.add("VIEW");
        reservedWords.add("WAITFOR");
        reservedWords.add("WHEN");
        reservedWords.add("WHERE");
        reservedWords.add("WHILE");
        reservedWords.add("WITH");
        reservedWords.add("WRITETEXT");
    }

}
