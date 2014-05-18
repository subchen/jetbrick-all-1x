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
package jetbrick.dao.dialect;

import java.util.*;
import jetbrick.dao.dialect.supports.*;
import jetbrick.lang.StringUtils;

public abstract class SqlDialect {
    private static final Map<String, SqlDialect> dialect_map = new HashMap<String, SqlDialect>();
    private static final Set<String> iso_reservedWords = new HashSet<String>(512);
    protected final Set<String> reservedWords = new HashSet<String>(256);

    static {
        MySqlDialect mysql = new MySqlDialect();
        dialect_map.put(MySqlDialect.NAME.toLowerCase(), mysql);
        dialect_map.put(mysql.getName().toLowerCase(), mysql);

        H2Dialect h2 = new H2Dialect();
        dialect_map.put(H2Dialect.NAME.toLowerCase(), h2);
        dialect_map.put(h2.getName().toLowerCase(), h2);

        SqlServerDialect sqlserver = new SqlServerDialect();
        dialect_map.put(SqlServerDialect.NAME.toLowerCase(), sqlserver);
        dialect_map.put(sqlserver.getName().toLowerCase(), sqlserver);

        OracleDialect oracle = new OracleDialect();
        dialect_map.put(OracleDialect.NAME.toLowerCase(), oracle);
        dialect_map.put(oracle.getName().toLowerCase(), oracle);

        init_sql92_reservedWords();
        init_sql99_reservedWords();
    }

    public static SqlDialect getDialect(String name) {
        SqlDialect dialect = (name == null) ? null : dialect_map.get(name.toLowerCase());
        if (dialect == null) {
            throw new IllegalStateException("Unsupport database, dialect.name = " + name);
        }
        return dialect;
    }

    public SqlDialect() {
        initializeReservedWords();
    }

    public String getName() {
        String name = getClass().getSimpleName().toLowerCase();
        return StringUtils.remove(name, "dialect");
    }

    protected abstract void initializeReservedWords();

    /**
     * 返回对应的 Hibernate 的 Dialect
     */
    public abstract String getHibernateDialect();

    /**
     * 数据库是否支持 Sequence
     */
    public boolean supportsSequences() {
        return false;
    }

    /**
     * 是否支持在添加字段的时候，指定字段位置
     */
    public boolean supportsColumnPosition() {
        return false;
    }

    /**
     * 哪些数据库字段运行指定长度.
     * @param type 具体的数据库字段类型
     */
    public boolean supportsColumnLength(String type) {
        Set<String> columnSet = new HashSet<String>();
        columnSet.add("char");
        columnSet.add("nchar");
        columnSet.add("varchar");
        columnSet.add("nvarchar");
        columnSet.add("varchar2");
        columnSet.add("nvarchar2");
        columnSet.add("number");
        columnSet.add("numeric");
        columnSet.add("dec");
        columnSet.add("decimal");
        return columnSet.contains(type.toLowerCase());
    }

    /**
     * 哪些数据库字段运行指定精度.
     * @param type 具体的数据库字段类型
     */
    public boolean supportsColumnScale(String type) {
        Set<String> columnSet = new HashSet<String>();
        columnSet.add("number");
        columnSet.add("numeric");
        columnSet.add("dec");
        columnSet.add("decimal");
        return columnSet.contains(type.toLowerCase());
    }

    /**
     * 将数据库无关的 SubStyleType 转换成具体数据库的字段名称。特殊的字段类型由子类实现。
     * @param type  数据库无关的字段名称，参考 {@link SubStyleType}
     */
    public String asSqlType(String type, Integer length, Integer scale) {
        if (SubStyleType.UID.equals(type)) {
            return "bigint";
        } else if (SubStyleType.UUID.equals(type)) {
            return "char(16)";
        } else if (SubStyleType.ENUM.equals(type)) {
            return "integer";
        } else if (SubStyleType.INT.equals(type)) {
            return "integer";
        } else if (SubStyleType.LONG.equals(type)) {
            return "bigint";
        } else if (SubStyleType.BIGINT.equals(type)) {
            return "decimal(38, 0)";
        } else if (SubStyleType.DOUBLE.equals(type)) {
            return "double";
        } else if (SubStyleType.DECIMAL.equals(type)) {
            return new SqlType("decimal", length, scale).toString();
        } else if (SubStyleType.CHAR.equals(type)) {
            return new SqlType("char", length, null).toString();
        } else if (SubStyleType.VARCHAR.equals(type)) {
            return new SqlType("varchar", length, null).toString();
        } else if (SubStyleType.TEXT.equals(type)) {
            return new SqlType("longvarchar", Integer.MAX_VALUE, null).toString();
        } else if (SubStyleType.BOOLEAN.equals(type)) {
            return "tinyint(1)";
        } else if (SubStyleType.DATETIME_STRING.equals(type)) {
            return "char(19)";
        } else if (SubStyleType.DATE_STRING.equals(type)) {
            return "char(10)";
        } else if (SubStyleType.TIME_STRING.equals(type)) {
            return "char(8)";
        } else if (SubStyleType.DATETIME.equals(type)) {
            return "timestamp";
        } else if (SubStyleType.TIMESTAMP.equals(type)) {
            return "timestamp";
        } else if (SubStyleType.DATE.equals(type)) {
            return "date";
        } else if (SubStyleType.TIME.equals(type)) {
            return "time";
        } else if (SubStyleType.CLOB.equals(type)) {
            return "clob";
        } else if (SubStyleType.BLOB.equals(type)) {
            return "blob";
        } else if (SubStyleType.INPUTSTREAM.equals(type)) {
            return "longvarbinary";
        } else {
            return new SqlType(type, length, scale).toString();
        }
    }

    /**
     * 将字段名/表名与SQL保留字冲突的名称进行 Wrapper
     */
    public String getIdentifier(String name) {
        String upperCaseName = name.toUpperCase();
        if (iso_reservedWords.contains(upperCaseName)) {
            return getQuotedIdentifier(name);
        }
        if (reservedWords.contains(upperCaseName)) {
            return getQuotedIdentifier(name);
        }
        return name;
    }

    /**
     * 将字段名/表名进行 Wrapper
     */
    protected String getQuotedIdentifier(String name) {
        return name;
    }

    /**
     * SQL 语句中的字符串值，默认编码单引号(')为双单引号(')
     */
    protected String escapeSqlValue(String value) {
        return StringUtils.replace(value, "'", "''");
    }

    /**
     * 批量运行多个SQL语句之间的分隔符。
     */
    public String getSqlStatmentSeparator() {
        return "";
    }

    public abstract String sql_table_drop(String table);

    public abstract String sql_table_rename(String oldName, String newName);

    public abstract String sql_column_add(String table, String column_definition, String column_position);

    public abstract String sql_column_modify(String table, String column_definition, String column_position);

    public abstract String sql_column_drop(String table, String column);

    /**
     * 生成分页sql
     * @param sql 原始sql
     * @param offset 开始位置，从0开始 （= (Page-1)*PageSize）
     * @param limit 返回的限制大小（= 分页大小 PageSize）
     * @return 如果不支持，返回 null
     */
    public String sql_pagelist(String sql, int offset, int limit) {
        return null;
    }

    private static void init_sql92_reservedWords() {
        iso_reservedWords.add("ABSOLUTE");
        iso_reservedWords.add("ACTION");
        iso_reservedWords.add("ADD");
        iso_reservedWords.add("ALL");
        iso_reservedWords.add("ALLOCATE");
        iso_reservedWords.add("ALTER");
        iso_reservedWords.add("AND");
        iso_reservedWords.add("ANY");
        iso_reservedWords.add("ARE");
        iso_reservedWords.add("AS");
        iso_reservedWords.add("ASC");
        iso_reservedWords.add("ASSERTION");
        iso_reservedWords.add("AT");
        iso_reservedWords.add("AUTHORIZATION");
        iso_reservedWords.add("AVG");
        iso_reservedWords.add("BEGIN");
        iso_reservedWords.add("BETWEEN");
        iso_reservedWords.add("BIT");
        iso_reservedWords.add("BIT_LENGTH");
        iso_reservedWords.add("BOTH");
        iso_reservedWords.add("BY");
        iso_reservedWords.add("CASCADE");
        iso_reservedWords.add("CASCADED");
        iso_reservedWords.add("CASE");
        iso_reservedWords.add("CAST");
        iso_reservedWords.add("CATALOG");
        iso_reservedWords.add("CHAR");
        iso_reservedWords.add("CHARACTER");
        iso_reservedWords.add("CHARACTER_LENGTH");
        iso_reservedWords.add("CHAR_LENGTH");
        iso_reservedWords.add("CHECK");
        iso_reservedWords.add("CLOSE");
        iso_reservedWords.add("COALESCE");
        iso_reservedWords.add("COLLATE");
        iso_reservedWords.add("COLLATION");
        iso_reservedWords.add("COLUMN");
        iso_reservedWords.add("COMMIT");
        iso_reservedWords.add("CONNECT");
        iso_reservedWords.add("CONNECTION");
        iso_reservedWords.add("CONSTRAINT");
        iso_reservedWords.add("CONSTRAINTS");
        iso_reservedWords.add("CONTINUE");
        iso_reservedWords.add("CONVERT");
        iso_reservedWords.add("CORRESPONDING");
        iso_reservedWords.add("COUNT");
        iso_reservedWords.add("CREATE");
        iso_reservedWords.add("CROSS");
        iso_reservedWords.add("CURRENT");
        iso_reservedWords.add("CURRENT_DATE");
        iso_reservedWords.add("CURRENT_TIME");
        iso_reservedWords.add("CURRENT_TIMESTAMP");
        iso_reservedWords.add("CURRENT_USER");
        iso_reservedWords.add("CURSOR");
        iso_reservedWords.add("DATE");
        iso_reservedWords.add("DAY");
        iso_reservedWords.add("DEALLOCATE");
        iso_reservedWords.add("DEC");
        iso_reservedWords.add("DECIMAL");
        iso_reservedWords.add("DECLARE");
        iso_reservedWords.add("DEFAULT");
        iso_reservedWords.add("DEFERRABLE");
        iso_reservedWords.add("DEFERRED");
        iso_reservedWords.add("DELETE");
        iso_reservedWords.add("DESC");
        iso_reservedWords.add("DESCRIBE");
        iso_reservedWords.add("DESCRIPTOR");
        iso_reservedWords.add("DIAGNOSTICS");
        iso_reservedWords.add("DISCONNECT");
        iso_reservedWords.add("DISTINCT");
        iso_reservedWords.add("DOMAIN");
        iso_reservedWords.add("DOUBLE");
        iso_reservedWords.add("DROP");
        iso_reservedWords.add("ELSE");
        iso_reservedWords.add("END");
        iso_reservedWords.add("END-EXEC");
        iso_reservedWords.add("ESCAPE");
        iso_reservedWords.add("EXCEPT");
        iso_reservedWords.add("EXCEPTION");
        iso_reservedWords.add("EXEC");
        iso_reservedWords.add("EXECUTE");
        iso_reservedWords.add("EXISTS");
        iso_reservedWords.add("EXTERNAL");
        iso_reservedWords.add("EXTRACT");
        iso_reservedWords.add("FALSE");
        iso_reservedWords.add("FETCH");
        iso_reservedWords.add("FIRST");
        iso_reservedWords.add("FLOAT");
        iso_reservedWords.add("FOR");
        iso_reservedWords.add("FOREIGN");
        iso_reservedWords.add("FOUND");
        iso_reservedWords.add("FROM");
        iso_reservedWords.add("FULL");
        iso_reservedWords.add("GET");
        iso_reservedWords.add("GLOBAL");
        iso_reservedWords.add("GO");
        iso_reservedWords.add("GOTO");
        iso_reservedWords.add("GRANT");
        iso_reservedWords.add("GROUP");
        iso_reservedWords.add("HAVING");
        iso_reservedWords.add("HOUR");
        iso_reservedWords.add("IDENTITY");
        iso_reservedWords.add("IMMEDIATE");
        iso_reservedWords.add("IN");
        iso_reservedWords.add("INDICATOR");
        iso_reservedWords.add("INITIALLY");
        iso_reservedWords.add("INNER");
        iso_reservedWords.add("INPUT");
        iso_reservedWords.add("INSENSITIVE");
        iso_reservedWords.add("INSERT");
        iso_reservedWords.add("INT");
        iso_reservedWords.add("INTEGER");
        iso_reservedWords.add("INTERSECT");
        iso_reservedWords.add("INTERVAL");
        iso_reservedWords.add("INTO");
        iso_reservedWords.add("IS");
        iso_reservedWords.add("ISOLATION");
        iso_reservedWords.add("JOIN");
        iso_reservedWords.add("KEY");
        iso_reservedWords.add("LANGUAGE");
        iso_reservedWords.add("LAST");
        iso_reservedWords.add("LEADING");
        iso_reservedWords.add("LEFT");
        iso_reservedWords.add("LEVEL");
        iso_reservedWords.add("LIKE");
        iso_reservedWords.add("LOCAL");
        iso_reservedWords.add("LOWER");
        iso_reservedWords.add("MATCH");
        iso_reservedWords.add("MAX");
        iso_reservedWords.add("MIN");
        iso_reservedWords.add("MINUTE");
        iso_reservedWords.add("MODULE");
        iso_reservedWords.add("MONTH");
        iso_reservedWords.add("NAMES");
        iso_reservedWords.add("NATIONAL");
        iso_reservedWords.add("NATURAL");
        iso_reservedWords.add("NCHAR");
        iso_reservedWords.add("NEXT");
        iso_reservedWords.add("NO");
        iso_reservedWords.add("NOT");
        iso_reservedWords.add("NULL");
        iso_reservedWords.add("NULLIF");
        iso_reservedWords.add("NUMERIC");
        iso_reservedWords.add("OCTET_LENGTH");
        iso_reservedWords.add("OF");
        iso_reservedWords.add("ON");
        iso_reservedWords.add("ONLY");
        iso_reservedWords.add("OPEN");
        iso_reservedWords.add("OPTION");
        iso_reservedWords.add("OR");
        iso_reservedWords.add("ORDER");
        iso_reservedWords.add("OUTER");
        iso_reservedWords.add("OUTPUT");
        iso_reservedWords.add("OVERLAPS");
        iso_reservedWords.add("PAD");
        iso_reservedWords.add("PARTIAL");
        iso_reservedWords.add("POSITION");
        iso_reservedWords.add("PRECISION");
        iso_reservedWords.add("PREPARE");
        iso_reservedWords.add("PRESERVE");
        iso_reservedWords.add("PRIMARY");
        iso_reservedWords.add("PRIOR");
        iso_reservedWords.add("PRIVILEGES");
        iso_reservedWords.add("PROCEDURE");
        iso_reservedWords.add("PUBLIC");
        iso_reservedWords.add("READ");
        iso_reservedWords.add("REAL");
        iso_reservedWords.add("REFERENCES");
        iso_reservedWords.add("RELATIVE");
        iso_reservedWords.add("RESTRICT");
        iso_reservedWords.add("REVOKE");
        iso_reservedWords.add("RIGHT");
        iso_reservedWords.add("ROLLBACK");
        iso_reservedWords.add("ROWS");
        iso_reservedWords.add("SCHEMA");
        iso_reservedWords.add("SCROLL");
        iso_reservedWords.add("SECOND");
        iso_reservedWords.add("SECTION");
        iso_reservedWords.add("SELECT");
        iso_reservedWords.add("SESSION");
        iso_reservedWords.add("SESSION_USER");
        iso_reservedWords.add("SET");
        iso_reservedWords.add("SIZE");
        iso_reservedWords.add("SMALLINT");
        iso_reservedWords.add("SOME");
        iso_reservedWords.add("SPACE");
        iso_reservedWords.add("SQL");
        iso_reservedWords.add("SQLCODE");
        iso_reservedWords.add("SQLERROR");
        iso_reservedWords.add("SQLSTATE");
        iso_reservedWords.add("SUBSTRING");
        iso_reservedWords.add("SUM");
        iso_reservedWords.add("SYSTEM_USER");
        iso_reservedWords.add("TABLE");
        iso_reservedWords.add("TEMPORARY");
        iso_reservedWords.add("THEN");
        iso_reservedWords.add("TIME");
        iso_reservedWords.add("DATETIME");
        iso_reservedWords.add("TIMEZONE_HOUR");
        iso_reservedWords.add("TIMEZONE_MINUTE");
        iso_reservedWords.add("TO");
        iso_reservedWords.add("TRAILING");
        iso_reservedWords.add("TRANSACTION");
        iso_reservedWords.add("TRANSLATE");
        iso_reservedWords.add("TRANSLATION");
        iso_reservedWords.add("TRIM");
        iso_reservedWords.add("TRUE");
        iso_reservedWords.add("UNION");
        iso_reservedWords.add("UNIQUE");
        iso_reservedWords.add("UNKNOWN");
        iso_reservedWords.add("UPDATE");
        iso_reservedWords.add("UPPER");
        iso_reservedWords.add("USAGE");
        iso_reservedWords.add("USER");
        iso_reservedWords.add("USING");
        iso_reservedWords.add("VALUE");
        iso_reservedWords.add("VALUES");
        iso_reservedWords.add("VARCHAR");
        iso_reservedWords.add("VARYING");
        iso_reservedWords.add("VIEW");
        iso_reservedWords.add("WHEN");
        iso_reservedWords.add("WHENEVER");
        iso_reservedWords.add("WHERE");
        iso_reservedWords.add("WITH");
        iso_reservedWords.add("WORK");
        iso_reservedWords.add("WRITE");
        iso_reservedWords.add("YEAR");
        iso_reservedWords.add("ZONE");
    }

    private static void init_sql99_reservedWords() {
        iso_reservedWords.add("ADMIN");
        iso_reservedWords.add("AFTER");
        iso_reservedWords.add("AGGREGATE");
        iso_reservedWords.add("ALIAS");
        iso_reservedWords.add("ARRAY");
        iso_reservedWords.add("BEFORE");
        iso_reservedWords.add("BINARY");
        iso_reservedWords.add("BLOB");
        iso_reservedWords.add("BOOLEAN");
        iso_reservedWords.add("BREADTH");
        iso_reservedWords.add("CALL");
        iso_reservedWords.add("CLASS");
        iso_reservedWords.add("CLOB");
        iso_reservedWords.add("COMPLETION");
        iso_reservedWords.add("CONSTRUCTOR");
        iso_reservedWords.add("CUBE");
        iso_reservedWords.add("CURRENT_PATH");
        iso_reservedWords.add("CURRENT_ROLE");
        iso_reservedWords.add("CYCLE");
        iso_reservedWords.add("DATA");
        iso_reservedWords.add("DEPTH");
        iso_reservedWords.add("DEREF");
        iso_reservedWords.add("DESTROY");
        iso_reservedWords.add("DESTRUCTOR");
        iso_reservedWords.add("DETERMINISTIC");
        iso_reservedWords.add("DICTIONARY");
        iso_reservedWords.add("DYNAMIC");
        iso_reservedWords.add("EACH");
        iso_reservedWords.add("EQUALS");
        iso_reservedWords.add("EVERY");
        iso_reservedWords.add("FREE");
        iso_reservedWords.add("FUNCTION");
        iso_reservedWords.add("GENERAL");
        iso_reservedWords.add("GROUPING");
        iso_reservedWords.add("HOST");
        iso_reservedWords.add("IGNORE");
        iso_reservedWords.add("INITIALIZE");
        iso_reservedWords.add("INOUT");
        iso_reservedWords.add("ITERATE");
        iso_reservedWords.add("LARGE");
        iso_reservedWords.add("LATERAL");
        iso_reservedWords.add("LESS");
        iso_reservedWords.add("LIMIT");
        iso_reservedWords.add("LOCALTIME");
        iso_reservedWords.add("LOCALTIMESTAMP");
        iso_reservedWords.add("LOCATOR");
        iso_reservedWords.add("MAP");
        iso_reservedWords.add("MODIFIES");
        iso_reservedWords.add("MODIFY");
        iso_reservedWords.add("NCLOB");
        iso_reservedWords.add("NEW");
        iso_reservedWords.add("NONE");
        iso_reservedWords.add("OBJECT");
        iso_reservedWords.add("OFF");
        iso_reservedWords.add("OLD");
        iso_reservedWords.add("OPERATION");
        iso_reservedWords.add("ORDINALITY");
        iso_reservedWords.add("OUT");
        iso_reservedWords.add("PARAMETER");
        iso_reservedWords.add("PARAMETERS");
        iso_reservedWords.add("PATH");
        iso_reservedWords.add("POSTFIX");
        iso_reservedWords.add("PREFIX");
        iso_reservedWords.add("PREORDER");
        iso_reservedWords.add("READS");
        iso_reservedWords.add("RECURSIVE");
        iso_reservedWords.add("REF");
        iso_reservedWords.add("REFERENCING");
        iso_reservedWords.add("RESULT");
        iso_reservedWords.add("RETURN");
        iso_reservedWords.add("RETURNS");
        iso_reservedWords.add("ROLE");
        iso_reservedWords.add("ROLLUP");
        iso_reservedWords.add("ROUTINE");
        iso_reservedWords.add("ROW");
        iso_reservedWords.add("SAVEPOINT");
        iso_reservedWords.add("SCOPE");
        iso_reservedWords.add("SEARCH");
        iso_reservedWords.add("SEQUENCE");
        iso_reservedWords.add("SETS");
        iso_reservedWords.add("SPECIFIC");
        iso_reservedWords.add("SPECIFICTYPE");
        iso_reservedWords.add("SQLEXCEPTION");
        iso_reservedWords.add("SQLWARNING");
        iso_reservedWords.add("START");
        iso_reservedWords.add("STATE");
        iso_reservedWords.add("STATEMENT");
        iso_reservedWords.add("STATIC");
        iso_reservedWords.add("STRUCTURE");
        iso_reservedWords.add("TERMINATE");
        iso_reservedWords.add("THAN");
        iso_reservedWords.add("TREAT");
        iso_reservedWords.add("TRIGGER");
        iso_reservedWords.add("UNDER");
        iso_reservedWords.add("UNNEST");
        iso_reservedWords.add("VARIABLE");
        iso_reservedWords.add("WITHOUT");
    }

}
