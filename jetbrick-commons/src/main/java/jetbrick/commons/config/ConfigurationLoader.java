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

import java.io.*;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import jetbrick.commons.beans.ClassLoaderUtils;
import jetbrick.commons.io.IoUtils;

public final class ConfigurationLoader {
    private static final Pattern PLACE_HOLDER_PATTERN = Pattern.compile("\\$\\{([^}]*)\\}");
    private final Configuration config = new Configuration();

    public ConfigurationLoader load(File file) {
        try {
            return load(new FileInputStream(file), file.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ConfigurationLoader loadClasspath(String classpath) {
        if (classpath.startsWith("/")) {
            classpath = classpath.substring(1);
        }
        InputStream is = ClassLoaderUtils.getDefault().getResourceAsStream(classpath);
        return load(is, classpath);
    }

    public ConfigurationLoader load(String location) {
        if (location.startsWith("classpath:")) {
            location = location.substring("classpath:".length());
            return loadClasspath(location);
        } else if (location.startsWith("file:")) {
            location = location.substring("file:".length());
            return load(new File(location));
        } else {
            return load(new File(location));
        }
    }

    public ConfigurationLoader loadSerlvetResource(ServletContext sc, String location) {
        if (location.startsWith("classpath:")) {
            location = location.substring("classpath:".length());
            return loadClasspath(location);
        } else if (location.startsWith("file:")) {
            location = location.substring("file:".length());
            return load(new File(location));
        } else {
            if (!location.startsWith("/")) {
                location = "/" + location;
            }
            InputStream is = sc.getResourceAsStream(location);
            return load(is, location);
        }
    }

    private ConfigurationLoader load(InputStream is, String name) {
        if (is == null) {
            throw new IllegalStateException("InputStream not found: " + name);
        }
        name = name.toLowerCase();
        if (name.endsWith(".xml")) {
            return loadXml(is);
        } else if (name.endsWith(".props")) {
            return loadExtend(is);
        } else {
            return load(is);
        }
    }

    public ConfigurationLoader load(InputStream is) {
        if (is == null) return this;
        try {
            Properties config = new Properties();
            config.load(is);
            return load(config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IoUtils.closeQuietly(is);
        }
    }

    public ConfigurationLoader loadExtend(InputStream is) {
        Properties config = ExtendPropertiesLoader.load(is);
        return load(config);
    }

    public ConfigurationLoader loadXml(InputStream is) {
        Properties config = XmlPropertiesLoader.loadXml(is, false);
        return load(config);
    }

    public ConfigurationLoader load(Properties props) {
        if (config == null) return this;
        config.addAll(props);
        return this;
    }

    public ConfigurationLoader load(Map<String, String> props) {
        if (config == null) return this;
        config.addAll(props);
        return this;
    }

    public ConfigurationLoader load(String name, String value) {
        config.put(name, value);
        return this;
    }

    public ConfigurationLoader loadSystemProperties() {
        return load(System.getProperties());
    }

    public ConfigurationLoader loadSystemEnvs() {
        return load(System.getenv());
    }

    public ConfigurationLoader placeholder() {
        for (Map.Entry<String, String> entry : config.entrySet()) {
            String value = entry.getValue();

            if (value.contains("${")) {
                Matcher matcher = PLACE_HOLDER_PATTERN.matcher(value);
                StringBuffer sb = new StringBuffer();
                while (matcher.find()) {
                    String val = config.get(matcher.group(1));
                    if (val == null) {
                        throw new IllegalStateException("cannot find " + value);
                    }
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(val));
                }
                matcher.appendTail(sb);
                // reset value
                entry.setValue(sb.toString());
            }
        }
        return this;
    }

    public Configuration getConfiguration() {
        return config;
    }

    public Properties getProperties() {
        return config.toProperties();
    }
}
