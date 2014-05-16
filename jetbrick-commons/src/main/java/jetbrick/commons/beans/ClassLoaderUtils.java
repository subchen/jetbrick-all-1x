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
package jetbrick.commons.beans;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import jetbrick.commons.lang.StringUtils;

public class ClassLoaderUtils {
    private static final Map<String, String> abbreviationMap;

    /**
     * Returns current thread's context class loader
     */
    public static ClassLoader getDefault() {
        ClassLoader loader = null;
        try {
            loader = Thread.currentThread().getContextClassLoader();
        } catch (Throwable e) {
        }
        if (loader == null) {
            loader = ClassLoaderUtils.class.getClassLoader();
        }
        return loader;
    }

    /**
     * 使用默认的 ClassLoader 去载入类.
     * @throws ClassNotFoundException
     */
    public static Class<?> loadClass(final String qualifiedClassName) throws ClassNotFoundException {
        return loadClass(qualifiedClassName, null);
    }

    /**
     * 使用指定的 ClassLoader 去载入类.
     * @throws ClassNotFoundException
     */
    public static Class<?> loadClass(final String qualifiedClassName, final ClassLoader classLoader) throws ClassNotFoundException {
        if (qualifiedClassName == null) {
            throw new NullPointerException("qualifiedClassName must not be null.");
        }

        ClassLoader loader = (classLoader == null) ? getDefault() : classLoader;

        // 尝试基本类型
        if (abbreviationMap.containsKey(qualifiedClassName)) {
            String klassName = '[' + abbreviationMap.get(qualifiedClassName);
            return Class.forName(klassName, false, loader).getComponentType();
        }

        // 尝试用 Class.forName()
        try {
            String klassName = getCanonicalClassName(qualifiedClassName);
            return Class.forName(klassName, false, loader);
        } catch (ClassNotFoundException e) {
        }

        // 尝试当做一个内部类去识别
        if (qualifiedClassName.indexOf('$') == -1) {
            int ipos = qualifiedClassName.lastIndexOf('.');
            if (ipos > 0) {
                try {
                    String klassName = qualifiedClassName.substring(0, ipos) + '$' + qualifiedClassName.substring(ipos + 1);
                    klassName = getCanonicalClassName(klassName);
                    return Class.forName(klassName, false, loader);
                } catch (ClassNotFoundException e) {
                }
            }
        }

        throw new ClassNotFoundException("Class not found: " + qualifiedClassName);
    }

    /**
     * 将 Java 类名转为 {@code Class.forName()} 可以载入的类名格式.
     * <pre>
     * getCanonicalClassName("int") == "int";
     * getCanonicalClassName("int[]") == "[I";
     * getCanonicalClassName("java.lang.String") == "java.lang.String";
     * getCanonicalClassName("java.lang.String[]") == "[Ljava.lang.String;";
     * </pre>
     */
    public static String getCanonicalClassName(String qualifiedClassName) {
        if (qualifiedClassName == null) {
            throw new NullPointerException("qualifiedClassName must not be null.");
        }

        String name = StringUtils.deleteWhitespace(qualifiedClassName);
        if (name.endsWith("[]")) {
            StringBuilder sb = new StringBuilder();

            while (name.endsWith("[]")) {
                name = name.substring(0, name.length() - 2);
                sb.append('[');
            }

            String abbreviation = abbreviationMap.get(name);
            if (abbreviation != null) {
                sb.append(abbreviation);
            } else {
                sb.append('L').append(name).append(';');
            }

            name = sb.toString();
        }
        return name;
    }

    public static URL getResource(String resourceName) {
        return getResource(resourceName, null);
    }

    public static URL getResource(String resourceName, ClassLoader classLoader) {
        if (resourceName.startsWith("/")) {
            resourceName = resourceName.substring(1);
        }
        if (classLoader != null) {
            URL url = classLoader.getResource(resourceName);
            if (url != null) {
                return url;
            }
        }
        ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
        if (currentThreadClassLoader != null && currentThreadClassLoader != classLoader) {
            URL url = currentThreadClassLoader.getResource(resourceName);
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    public static InputStream getResourceAsStream(String resourceName) throws IOException {
        return getResourceAsStream(resourceName, null);
    }

    public static InputStream getResourceAsStream(String resourceName, ClassLoader callingClass) throws IOException {
        URL url = getResource(resourceName, callingClass);
        if (url != null) {
            return url.openStream();
        }
        return null;
    }

    public static InputStream getClassAsStream(Class<?> clazz) throws IOException {
        return getResourceAsStream(getClassFileName(clazz), clazz.getClassLoader());
    }

    public static InputStream getClassAsStream(String className) throws IOException {
        return getResourceAsStream(getClassFileName(className));
    }

    public static String getClassFileName(Class<?> clazz) {
        if (clazz.isArray()) {
            clazz = clazz.getComponentType();
        }
        return getClassFileName(clazz.getName());
    }

    public static String getClassFileName(String className) {
        return className.replace('.', '/') + ".class";
    }

    static {
        abbreviationMap = new HashMap<String, String>();
        abbreviationMap.put("boolean", "Z");
        abbreviationMap.put("byte", "B");
        abbreviationMap.put("short", "S");
        abbreviationMap.put("char", "C");
        abbreviationMap.put("int", "I");
        abbreviationMap.put("long", "J");
        abbreviationMap.put("float", "F");
        abbreviationMap.put("double", "D");
    }
}
