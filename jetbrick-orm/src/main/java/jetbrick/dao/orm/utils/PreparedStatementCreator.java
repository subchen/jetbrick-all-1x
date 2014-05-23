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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jetbrick.beans.ClassUtils;
import jetbrick.collections.iterators.ArrayIterator;
import jetbrick.lang.StringUtils;
import jetbrick.reflect.KlassInfo;

public class PreparedStatementCreator {
    private static final Pattern namedParameterPattern = Pattern.compile("\\:([a-zA-Z0-9_]+)");

    @SuppressWarnings("unchecked")
    public static PreparedStatement createPreparedStatement(Connection conn, String sql, Object... parameters) throws SQLException {
        if (parameters == null) {
            return createByIterator(conn, sql, null);
        }

        if (parameters.length == 1) {
            Object value = parameters[0];
            Class<?> clazz = value.getClass();
            if (ClassUtils.isAssignable(clazz, Map.class)) {
                return createByMap(conn, sql, (Map<String, ?>) value);
            } else if (ClassUtils.isAssignable(clazz, Collection.class)) {
                return createByIterator(conn, sql, new ArrayIterator(parameters));
            } else if (clazz.isPrimitive() || clazz.getName().startsWith("java.")) {
                return createByIterator(conn, sql, new ArrayIterator(parameters));
            } else {
                Map<String, Object> beanMap = KlassInfo.create(clazz).asBeanMap(value);
                return createByMap(conn, sql, beanMap);
            }
        } else {
            return createByIterator(conn, sql, new ArrayIterator(parameters));
        }
    }

    /**
     * Support ? as parameter
     */
    protected static PreparedStatement createByIterator(Connection conn, String sql, Iterator<?> parameters) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql);
        if (parameters != null) {
            int index = 1;
            while (parameters.hasNext()) {
                Object parameter = parameters.next();
                if (parameter == null) {
                    ps.setObject(index, null);
                } else {
                    ps.setObject(index, parameter);
                }
                index++;
            }
        }
        return ps;
    }

    /**
     * Support :name as parameter, and Array or Collection type
     */
    protected static PreparedStatement createByMap(Connection conn, String sql, Map<String, ?> parameters) throws SQLException {
        StringBuffer sb = new StringBuffer();
        List<Object> params = new ArrayList<Object>();

        Matcher m = namedParameterPattern.matcher(sql);
        while (m.find()) {
            String key = m.group(1);
            Object value = parameters.get(key);
            if (value == null) {
                params.add(null);
                m.appendReplacement(sb, "?");
            } else if (value instanceof Object[]) {
                Object[] array = (Object[]) value;
                if (array.length == 0) {
                    params.add(null);
                    m.appendReplacement(sb, "?");
                } else {
                    for (Object one : array) {
                        params.add(one);
                    }
                    m.appendReplacement(sb, StringUtils.repeat("?", ",", array.length));
                }
            } else if (value instanceof Collection) {
                Collection<?> collection = (Collection<?>) value;
                if (collection.size() == 0) {
                    params.add(null);
                    m.appendReplacement(sb, "?");
                } else {
                    for (Object one : collection) {
                        params.add(one);
                    }
                    m.appendReplacement(sb, StringUtils.repeat("?", ",", collection.size()));
                }
            } else {
                params.add(value);
                m.appendReplacement(sb, "?");
            }
        }
        m.appendTail(sb);

        return createByIterator(conn, sb.toString(), params.iterator());
    }

}
