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

import org.apache.log4j.Level;

public class Log4jLogger extends Logger {
    private static final String callerFQCN = Log4jLogger.class.getName();
    private final org.apache.log4j.Logger log;

    protected Log4jLogger(org.apache.log4j.Logger log) {
        this.log = log;
    }

    @Override
    public void debug(String message, Object... args) {
        log.log(callerFQCN, Level.DEBUG, format(message, args), null);
    }

    @Override
    public void debug(Object message) {
        log.log(callerFQCN, Level.DEBUG, format(message), null);
    }

    @Override
    public void info(String message, Object... args) {
        log.log(callerFQCN, Level.INFO, format(message, args), null);
    }

    @Override
    public void info(Object message) {
        log.log(callerFQCN, Level.INFO, format(message), null);
    }

    @Override
    public void warn(String message, Object... args) {
        log.log(callerFQCN, Level.WARN, format(message, args), null);
    }

    @Override
    public void warn(Throwable e) {
        log.log(callerFQCN, Level.WARN, e.getMessage(), e);
    }

    @Override
    public void warn(Throwable e, String message, Object... args) {
        log.log(callerFQCN, Level.WARN, format(message, args), e);
    }

    @Override
    public void error(String message, Object... args) {
        log.log(callerFQCN, Level.ERROR, format(message, args), null);
    }

    @Override
    public void error(Throwable e) {
        log.log(callerFQCN, Level.ERROR, e.getMessage(), e);
    }

    @Override
    public void error(Throwable e, String message, Object... args) {
        log.log(callerFQCN, Level.ERROR, format(message, args), e);
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return log.isEnabledFor(Level.WARN);
    }

    @Override
    public boolean isErrorEnabled() {
        return log.isEnabledFor(Level.ERROR);
    }
}
