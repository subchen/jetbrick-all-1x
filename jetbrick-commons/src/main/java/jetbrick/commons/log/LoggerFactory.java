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
package jetbrick.commons.log;

import jetbrick.commons.lang.JetServiceLoader;

public abstract class LoggerFactory {

    //@formatter:off
    private static final LoggerFactory factory = JetServiceLoader.load(
            LoggerFactory.class,
            "jetbrick.commons.log.Slf4jLoggerFactory, org.slf4j.Logger",
            "jetbrick.commons.log.Log4jLoggerFactory, org.apache.log4j.Logger",
            "jetbrick.commons.log.Jdk14LoggerFactory");
    //@formatter:on

    public static Logger getLogger(Class<?> clazz) {
        return factory.doGetLogger(clazz.getName());
    }

    public static Logger getLogger(String name) {
        return factory.doGetLogger(name);
    }

    protected abstract Logger doGetLogger(String name);
}
