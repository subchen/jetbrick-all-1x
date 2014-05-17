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
package jetbrick.lang;

import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import jetbrick.beans.ClassLoaderUtils;
import jetbrick.collections.ListUtils;

public final class Log4jConfigUtils {

    public static void resetConfiguration() {
        org.apache.log4j.LogManager.resetConfiguration();

        URL config = ClassLoaderUtils.getDefault().getResource("/log4j.properties");
        if (config != null) {
            org.apache.log4j.PropertyConfigurator.configure(config);
        }
    }

    /**
     * @param level
     *      OFF, FATAL, ERROR, WARN, INFO, DEBUG and ALL.
     */
    public static void setLevel(String logName, String level) {
        org.apache.log4j.Logger log = StringUtils.isEmpty(logName) ? org.apache.log4j.Logger.getRootLogger() : org.apache.log4j.Logger.getLogger(logName);
        log.setLevel(org.apache.log4j.Level.toLevel(level, org.apache.log4j.Level.DEBUG));
    }

    @SuppressWarnings("unchecked")
    public static List<org.apache.log4j.Logger> getLoggers() {
        Enumeration<org.apache.log4j.Logger> loggers = org.apache.log4j.Logger.getRootLogger().getLoggerRepository().getCurrentLoggers();
        return ListUtils.asList(loggers);
    }
}
