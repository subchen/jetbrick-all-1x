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
package jetbrick.log;

import java.util.logging.Level;

public class Jdk14Logger extends Logger {
    private final java.util.logging.Logger log;
    private final String name;

    protected Jdk14Logger(java.util.logging.Logger log, String name) {
        this.log = log;
        this.name = name;
    }

    @Override
    public void debug(String message, Object... args) {
        log.logp(Level.FINE, name, getSourceMethod(), format(message, args));
    }

    @Override
    public void debug(Object message) {
        log.logp(Level.FINE, name, getSourceMethod(), (message == null ? null : message.toString()));
    }

    @Override
    public void info(String message, Object... args) {
        log.logp(Level.INFO, name, getSourceMethod(), format(message, args));
    }

    @Override
    public void info(Object message) {
        log.logp(Level.INFO, name, getSourceMethod(), (message == null ? null : message.toString()));
    }

    @Override
    public void warn(String message, Object... args) {
        log.logp(Level.WARNING, name, getSourceMethod(), format(message, args));
    }

    @Override
    public void warn(Throwable e) {
        log.logp(Level.WARNING, name, getSourceMethod(), e.getMessage(), e);
    }

    @Override
    public void warn(Throwable e, String message, Object... args) {
        log.logp(Level.WARNING, name, getSourceMethod(), format(message, args), e);
    }

    @Override
    public void error(String message, Object... args) {
        log.logp(Level.SEVERE, name, getSourceMethod(), format(message, args));
    }

    @Override
    public void error(Throwable e) {
        log.logp(Level.SEVERE, name, getSourceMethod(), e.getMessage(), e);
    }

    @Override
    public void error(Throwable e, String message, Object... args) {
        log.logp(Level.SEVERE, name, getSourceMethod(), format(message, args), e);
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isLoggable(Level.FINE);
    }

    @Override
    public boolean isInfoEnabled() {
        return log.isLoggable(Level.INFO);
    }

    @Override
    public boolean isWarnEnabled() {
        return log.isLoggable(Level.WARNING);
    }

    @Override
    public boolean isErrorEnabled() {
        return log.isLoggable(Level.SEVERE);
    }

    private String getSourceMethod() {
        return Thread.currentThread().getStackTrace()[3].getMethodName();
    }
}
