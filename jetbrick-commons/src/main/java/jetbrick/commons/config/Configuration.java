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
package jetbrick.commons.config;

import java.io.File;
import java.lang.reflect.Array;
import java.util.*;
import jetbrick.commons.beans.ClassLoaderUtils;
import jetbrick.commons.io.resource.Resource;
import jetbrick.commons.lang.StringUtils;
import jetbrick.commons.typecast.TypeCastUtils;

public class Configuration extends HashMap<String, String> {
    private static final long serialVersionUID = 1L;

    public Configuration() {
        super();
    }

    public Configuration(int initialCapacity) {
        super(initialCapacity);
    }

    public Configuration(Configuration config) {
        super(config);
    }

    public Configuration(Properties config) {
        super(config.size());
        addAll(config);
    }

    // -----------------------------------------------------------
    public String add(String name, String value) {
        return put(name, value);
    }

    public void addAll(Map<String, String> config) {
        putAll(config);
    }

    public void addAll(Properties config) {
        for (Map.Entry<Object, Object> entry : config.entrySet()) {
            Object name = entry.getKey();
            Object value = entry.getValue();
            if (name instanceof String && value instanceof String) {
                put((String) name, (String) value);
            }
        }
    }

    // -----------------------------------------------------------
    public Configuration subset(String prefix) {
        return subset(prefix, true);
    }

    public Configuration subset(String prefix, boolean removePrefix) {
        Configuration result = new Configuration();
        if (!prefix.endsWith(".")) {
            prefix = prefix + '.';
        }
        for (Map.Entry<String, String> entry : entrySet()) {
            String name = entry.getKey();
            if (name.startsWith(prefix)) {
                String value = entry.getValue();
                if (value != null) {
                    if (removePrefix) {
                        name = name.substring(prefix.length());
                    }
                    result.put(name, value);
                }
            }
        }
        return result;
    }

    public Properties toProperties() {
        Properties config = new Properties();
        config.putAll(this);
        return config;
    }

    // -----------------------------------------------------------------
    public String asString(String name) {
        return getValue(name, String.class);
    }

    public String asString(String name, String defaultValue) {
        return getValue(name, String.class, defaultValue);
    }

    public int asIntValue(String name) {
        return getValue(name, Integer.class, 0);
    }

    public Integer asInt(String name) {
        return getValue(name, Integer.class);
    }

    public Integer asInt(String name, Integer defaultValue) {
        return getValue(name, Integer.class, defaultValue);
    }

    public long asLongValue(String name) {
        return getValue(name, Long.class, 0L);
    }

    public Long asLong(String name) {
        return getValue(name, Long.class);
    }

    public Long asLong(String name, Long defaultValue) {
        return getValue(name, Long.class, defaultValue);
    }

    public double asDoubleValue(String name) {
        return getValue(name, Double.class, 0.0D);
    }

    public Double asDouble(String name) {
        return getValue(name, Double.class);
    }

    public Double asDouble(String name, Double defaultValue) {
        return getValue(name, Double.class, defaultValue);
    }

    public boolean asBooleanValue(String name) {
        return getValue(name, Boolean.class, Boolean.FALSE);
    }

    public Boolean asBoolean(String name) {
        return getValue(name, Boolean.class);
    }

    public Boolean asBoolean(String name, Boolean defaultValue) {
        return getValue(name, Boolean.class, defaultValue);
    }

    public Date asDate(String name) {
        return getValue(name, Date.class);
    }

    public Date asDate(String name, Date defaultValue) {
        return getValue(name, Date.class, defaultValue);
    }

    public Class<?> asClass(String name) {
        return getValue(name, Class.class);
    }

    public Class<?> asClass(String name, Class<?> defaultValue) {
        return getValue(name, Class.class, defaultValue);
    }

    public <T> T asObject(String name, Class<T> klass) {
        return getValue(name, klass, null);
    }

    public File asFile(String name) {
        return getValue(name, File.class);
    }

    public File asFile(String name, File defaultValue) {
        return getValue(name, File.class, defaultValue);
    }

    public Resource asResource(String name) {
        return getValue(name, Resource.class);
    }

    public Resource asResource(String name, Resource defaultValue) {
        return getValue(name, Resource.class, defaultValue);
    }

    // -----------------------------------------------------------------
    public List<String> asStringList(String name) {
        return getValueList(name, String.class);
    }

    public List<Integer> asIntList(String name) {
        return getValueList(name, Integer.class);
    }

    public List<Long> asLongList(String name) {
        return getValueList(name, Long.class);
    }

    public List<Double> asDoubleList(String name) {
        return getValueList(name, Double.class);
    }

    public List<Boolean> asBooleanList(String name) {
        return getValueList(name, Boolean.class);
    }

    public List<Date> asDateList(String name) {
        return getValueList(name, Date.class);
    }

    @SuppressWarnings("rawtypes")
    public List<Class> asClassList(String name) {
        return getValueList(name, Class.class);
    }

    public <T> List<T> asObjectList(String name, Class<T> klass) {
        return getValueList(name, klass);
    }

    public List<File> asFileList(String name) {
        return getValueList(name, File.class);
    }

    public List<Resource> asResourceList(String name) {
        return getValueList(name, Resource.class);
    }

    // -----------------------------------------------------------------
    public <T> T getValue(String name, Class<T> type) {
        return getValue(name, type, null);
    }

    public <T> T getValue(String name, Class<T> type, T defaultValue) {
        String value = get(name);
        if (value == null) {
            return defaultValue;
        } else {
            return cast(value, type);
        }
    }

    public <T> T getValue(String name, Class<T> type, String defaultValue) {
        String value = get(name);
        if (value == null) {
            value = defaultValue;
        }
        if (value == null) {
            return null;
        } else {
            return cast(value, type);
        }
    }

    public <T> List<T> getValueList(String name, Class<T> elementType) {
        String valueList = get(name);
        if (valueList == null || valueList.length() == 0) {
            return Collections.emptyList();
        }
        String[] values = StringUtils.split(valueList, ',');
        List<T> results = new ArrayList<T>(values.length);
        for (String value : values) {
            value = value.trim();
            T object = cast(value, elementType);
            if (object == null) {
                throw new IllegalStateException("Cannot convert to " + elementType.getName() + " : " + value);
            }
            results.add(object);
        }
        return Collections.unmodifiableList(results);
    }

    public Object getValueArray(String name, Class<?> elementType) {
        String valueList = get(name);
        if (valueList == null || valueList.length() == 0) {
            return Array.newInstance(elementType, 0);
        }
        String[] values = StringUtils.split(valueList, ',');
        Object results = Array.newInstance(elementType, values.length);
        for (int i = 0; i < values.length; i++) {
            String value = values[i].trim();
            Object object = cast(value, elementType);
            if (object == null) {
                throw new IllegalStateException("Cannot convert to " + elementType.getName() + " : " + value);
            }
            Array.set(results, i, object);
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    protected <T> T cast(String value, Class<T> type) {
        try {
            return TypeCastUtils.convert(value, type);
        } catch (IllegalStateException e) {
            try {
                Class<?> klass = ClassLoaderUtils.loadClassEx(value);
                return (T) klass.newInstance();
            } catch (ClassNotFoundException ex) {
                throw e; // IllegalStateException
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
