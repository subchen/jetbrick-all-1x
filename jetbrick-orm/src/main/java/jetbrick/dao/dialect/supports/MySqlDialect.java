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

import jetbrick.dao.dialect.SqlDialect;
import jetbrick.dao.dialect.SubStyleType;
import jetbrick.lang.StringUtils;

public class MySqlDialect extends SqlDialect {
    public static final String NAME = "MySQL";

    @Override
    protected String getQuotedIdentifier(String name) {
        return "`" + name + "`";
    }

    @Override
    protected String escapeSqlValue(String value) {
        return StringUtils.replace(value, "'", "\\'");
    }

    @Override
    public String sql_table_drop(String table) {
        return String.format("drop table if exists %s;", getIdentifier(table));
    }

    @Override
    public String sql_table_rename(String oldName, String newName) {
        return String.format("rename table  %s to %s;", getIdentifier(oldName), getIdentifier(newName));
    }

    @Override
    public String sql_column_add(String table, String column_definition, String column_position) {
        String sql = String.format("alter table %s add %s", getIdentifier(table), column_definition);
        if (supportsColumnPosition() && column_position != null) {
            sql = sql + " " + column_position;
        }
        return sql + ";";
    }

    @Override
    public String sql_column_modify(String table, String column_definition, String column_position) {
        String sql = String.format("alter table %s modify %s", getIdentifier(table), column_definition);
        if (supportsColumnPosition() && column_position != null) {
            sql = sql + " " + column_position;
        }
        return sql + ";";
    }

    @Override
    public String sql_column_drop(String table, String column) {
        return String.format("alter table %s drop %s;", getIdentifier(table), getIdentifier(column));
    }

    @Override
    public String sql_pagelist(String sql, int offset, int limit) {
        if (offset > 0) {
            return sql + " limit " + offset + "," + limit;
        } else {
            return sql + " limit " + limit;
        }
    }

    @Override
    public boolean supportsColumnPosition() {
        return true;
    }

    @Override
    public String getHibernateDialect() {
        return "org.hibernate.dialect.MySQLDialect";
    }

    @Override
    public String asSqlType(String type, Integer length, Integer scale) {
        if (SubStyleType.TEXT.equals(type)) {
            return "longtext";
        } else if (SubStyleType.LONG.equals(type)) {
            return "bigint";
        } else if (SubStyleType.BIGINT.equals(type)) {
            return "decimal(64, 0)";
        } else if (SubStyleType.DATETIME.equals(type)) {
            return "datetime";
        } else if (SubStyleType.TIMESTAMP.equals(type)) {
            return "datetime";
        } else if (SubStyleType.BLOB.equals(type)) {
            return "longblob";
        }
        return super.asSqlType(type, length, scale);
    }

    @Override
    protected void initializeReservedWords() {
        reservedWords.add("ADD");
        reservedWords.add("ALL");
        reservedWords.add("ALTER");
        reservedWords.add("ANALYZE");
        reservedWords.add("AND");
        reservedWords.add("AS");
        reservedWords.add("ASC");
        reservedWords.add("ASENSITIVE");
        reservedWords.add("BEFORE");
        reservedWords.add("BETWEEN");
        reservedWords.add("BIGINT");
        reservedWords.add("BINARY");
        reservedWords.add("BLOB");
        reservedWords.add("BOTH");
        reservedWords.add("BY");
        reservedWords.add("CALL");
        reservedWords.add("CASCADE");
        reservedWords.add("CASE");
        reservedWords.add("CHANGE");
        reservedWords.add("CHAR");
        reservedWords.add("CHARACTER");
        reservedWords.add("CHECK");
        reservedWords.add("COLLATE");
        reservedWords.add("COLUMN");
        reservedWords.add("CONDITION");
        reservedWords.add("CONNECTION");
        reservedWords.add("CONSTRAINT");
        reservedWords.add("CONTINUE");
        reservedWords.add("CONVERT");
        reservedWords.add("CREATE");
        reservedWords.add("CROSS");
        reservedWords.add("CURRENT_DATE");
        reservedWords.add("CURRENT_TIME");
        reservedWords.add("CURRENT_TIMESTAMP");
        reservedWords.add("CURRENT_USER");
        reservedWords.add("CURSOR");
        reservedWords.add("DATABASE");
        reservedWords.add("DATABASES");
        reservedWords.add("DAY_HOUR");
        reservedWords.add("DAY_MICROSECOND");
        reservedWords.add("DAY_MINUTE");
        reservedWords.add("DAY_SECOND");
        reservedWords.add("DEC");
        reservedWords.add("DECIMAL");
        reservedWords.add("DECLARE");
        reservedWords.add("DEFAULT");
        reservedWords.add("DELAYED");
        reservedWords.add("DELETE");
        reservedWords.add("DESC");
        reservedWords.add("DESCRIBE");
        reservedWords.add("DETERMINISTIC");
        reservedWords.add("DISTINCT");
        reservedWords.add("DISTINCTROW");
        reservedWords.add("DIV");
        reservedWords.add("DOUBLE");
        reservedWords.add("DROP");
        reservedWords.add("DUAL");
        reservedWords.add("EACH");
        reservedWords.add("ELSE");
        reservedWords.add("ELSEIF");
        reservedWords.add("ENCLOSED");
        reservedWords.add("ESCAPED");
        reservedWords.add("EXISTS");
        reservedWords.add("EXIT");
        reservedWords.add("EXPLAIN");
        reservedWords.add("FALSE");
        reservedWords.add("FETCH");
        reservedWords.add("FLOAT");
        reservedWords.add("FLOAT4");
        reservedWords.add("FLOAT8");
        reservedWords.add("FOR");
        reservedWords.add("FORCE");
        reservedWords.add("FOREIGN");
        reservedWords.add("FROM");
        reservedWords.add("FULLTEXT");
        reservedWords.add("GOTO");
        reservedWords.add("GRANT");
        reservedWords.add("GROUP");
        reservedWords.add("HAVING");
        reservedWords.add("HIGH_PRIORITY");
        reservedWords.add("HOUR_MICROSECOND");
        reservedWords.add("HOUR_MINUTE");
        reservedWords.add("HOUR_SECOND");
        reservedWords.add("IF");
        reservedWords.add("IGNORE");
        reservedWords.add("IN");
        reservedWords.add("INDEX");
        reservedWords.add("INFILE");
        reservedWords.add("INNER");
        reservedWords.add("INOUT");
        reservedWords.add("INSENSITIVE");
        reservedWords.add("INSERT");
        reservedWords.add("INT");
        reservedWords.add("INT1");
        reservedWords.add("INT2");
        reservedWords.add("INT3");
        reservedWords.add("INT4");
        reservedWords.add("INT8");
        reservedWords.add("INTEGER");
        reservedWords.add("INTERVAL");
        reservedWords.add("INTO");
        reservedWords.add("IS");
        reservedWords.add("ITERATE");
        reservedWords.add("JOIN");
        reservedWords.add("KEY");
        reservedWords.add("KEYS");
        reservedWords.add("KILL");
        reservedWords.add("LABEL");
        reservedWords.add("LEADING");
        reservedWords.add("LEAVE");
        reservedWords.add("LEFT");
        reservedWords.add("LIKE");
        reservedWords.add("LIMIT");
        reservedWords.add("LINEAR");
        reservedWords.add("LINES");
        reservedWords.add("LOAD");
        reservedWords.add("LOCALTIME");
        reservedWords.add("LOCALTIMESTAMP");
        reservedWords.add("LOCK");
        reservedWords.add("LONG");
        reservedWords.add("LONGBLOB");
        reservedWords.add("LONGTEXT");
        reservedWords.add("LOOP");
        reservedWords.add("LOW_PRIORITY");
        reservedWords.add("MATCH");
        reservedWords.add("MEDIUMBLOB");
        reservedWords.add("MEDIUMINT");
        reservedWords.add("MEDIUMTEXT");
        reservedWords.add("MIDDLEINT");
        reservedWords.add("MINUTE_MICROSECOND");
        reservedWords.add("MINUTE_SECOND");
        reservedWords.add("MOD");
        reservedWords.add("MODIFIES");
        reservedWords.add("NATURAL");
        reservedWords.add("NOT");
        reservedWords.add("NO_WRITE_TO_BINLOG");
        reservedWords.add("NULL");
        reservedWords.add("NUMERIC");
        reservedWords.add("ON");
        reservedWords.add("OPTIMIZE");
        reservedWords.add("OPTION");
        reservedWords.add("OPTIONALLY");
        reservedWords.add("OR");
        reservedWords.add("ORDER");
        reservedWords.add("OUT");
        reservedWords.add("OUTER");
        reservedWords.add("OUTFILE");
        reservedWords.add("PRECISION");
        reservedWords.add("PRIMARY");
        reservedWords.add("PROCEDURE");
        reservedWords.add("PURGE");
        reservedWords.add("RAID0");
        reservedWords.add("RANGE");
        reservedWords.add("READ");
        reservedWords.add("READS");
        reservedWords.add("REAL");
        reservedWords.add("REFERENCES");
        reservedWords.add("REGEXP");
        reservedWords.add("RELEASE");
        reservedWords.add("RENAME");
        reservedWords.add("REPEAT");
        reservedWords.add("REPLACE");
        reservedWords.add("REQUIRE");
        reservedWords.add("RESTRICT");
        reservedWords.add("RETURN");
        reservedWords.add("REVOKE");
        reservedWords.add("RIGHT");
        reservedWords.add("RLIKE");
        reservedWords.add("SCHEMA");
        reservedWords.add("SCHEMAS");
        reservedWords.add("SECOND_MICROSECOND");
        reservedWords.add("SELECT");
        reservedWords.add("SENSITIVE");
        reservedWords.add("SEPARATOR");
        reservedWords.add("SET");
        reservedWords.add("SHOW");
        reservedWords.add("SMALLINT");
        reservedWords.add("SPATIAL");
        reservedWords.add("SPECIFIC");
        reservedWords.add("SQL");
        reservedWords.add("SQLEXCEPTION");
        reservedWords.add("SQLSTATE");
        reservedWords.add("SQLWARNING");
        reservedWords.add("SQL_BIG_RESULT");
        reservedWords.add("SQL_CALC_FOUND_ROWS");
        reservedWords.add("SQL_SMALL_RESULT");
        reservedWords.add("SSL");
        reservedWords.add("STARTING");
        reservedWords.add("STRAIGHT_JOIN");
        reservedWords.add("TABLE");
        reservedWords.add("TERMINATED");
        reservedWords.add("THEN");
        reservedWords.add("TINYBLOB");
        reservedWords.add("TINYINT");
        reservedWords.add("TINYTEXT");
        reservedWords.add("TO");
        reservedWords.add("TRAILING");
        reservedWords.add("TRIGGER");
        reservedWords.add("TRUE");
        reservedWords.add("UNDO");
        reservedWords.add("UNION");
        reservedWords.add("UNIQUE");
        reservedWords.add("UNLOCK");
        reservedWords.add("UNSIGNED");
        reservedWords.add("UPDATE");
        reservedWords.add("USAGE");
        reservedWords.add("USE");
        reservedWords.add("USING");
        reservedWords.add("UTC_DATE");
        reservedWords.add("UTC_TIME");
        reservedWords.add("UTC_TIMESTAMP");
        reservedWords.add("VALUES");
        reservedWords.add("VARBINARY");
        reservedWords.add("VARCHAR");
        reservedWords.add("VARCHARACTER");
        reservedWords.add("VARYING");
        reservedWords.add("WHEN");
        reservedWords.add("WHERE");
        reservedWords.add("WHILE");
        reservedWords.add("WITH");
        reservedWords.add("WRITE");
        reservedWords.add("X509");
        reservedWords.add("XOR");
        reservedWords.add("YEAR_MONTH");
        reservedWords.add("ZEROFILL");
    }

}
