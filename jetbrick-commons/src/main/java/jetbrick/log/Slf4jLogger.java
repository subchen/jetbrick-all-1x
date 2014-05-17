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

import org.slf4j.spi.LocationAwareLogger;

public class Slf4jLogger extends Logger {
    private static final String FQCN = Slf4jLogger.class.getName();

    private final org.slf4j.Logger log;
    private final LocationAwareLogger locationAwareLogger;

    public Slf4jLogger(org.slf4j.Logger log) {
        this.log = log;
        if (log instanceof LocationAwareLogger) {
            this.locationAwareLogger = (LocationAwareLogger) log;
        } else {
            this.locationAwareLogger = null;
        }
    }

    @Override
    public void debug(String message, Object... args) {
        if (locationAwareLogger == null) {
            log.debug(format(message, args));
        } else {
            locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, format(message, args), null, null);
        }
    }

    @Override
    public void debug(Object message) {
        if (locationAwareLogger == null) {
            log.debug(format(message));
        } else {
            locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, format(message), null, null);
        }
    }

    @Override
    public void info(String message, Object... args) {
        if (locationAwareLogger == null) {
            log.info(format(message, args));
        } else {
            locationAwareLogger.log(null, FQCN, LocationAwareLogger.INFO_INT, format(message, args), null, null);
        }
    }

    @Override
    public void info(Object message) {
        if (locationAwareLogger == null) {
            log.info(format(message));
        } else {
            locationAwareLogger.log(null, FQCN, LocationAwareLogger.INFO_INT, format(message), null, null);
        }
    }

    @Override
    public void warn(String message, Object... args) {
        if (locationAwareLogger == null) {
            log.warn(format(message, args));
        } else {
            locationAwareLogger.log(null, FQCN, LocationAwareLogger.WARN_INT, format(message, args), null, null);
        }
    }

    @Override
    public void warn(Throwable e) {
        if (locationAwareLogger == null) {
            log.warn(null, e);
        } else {
            locationAwareLogger.log(null, FQCN, LocationAwareLogger.WARN_INT, null, null, e);
        }
    }

    @Override
    public void warn(Throwable e, String message, Object... args) {
        if (locationAwareLogger == null) {
            log.warn(format(message, args), e);
        } else {
            locationAwareLogger.log(null, FQCN, LocationAwareLogger.WARN_INT, format(message, args), null, e);
        }
    }

    @Override
    public void error(String message, Object... args) {
        if (locationAwareLogger == null) {
            log.error(format(message, args));
        } else {
            locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, format(message, args), null, null);
        }
    }

    @Override
    public void error(Throwable e) {
        if (locationAwareLogger == null) {
            log.error(null, e);
        } else {
            locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, null, null, e);
        }
    }

    @Override
    public void error(Throwable e, String message, Object... args) {
        if (locationAwareLogger == null) {
            log.error(format(message, args), e);
        } else {
            locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, format(message, args), null, e);
        }
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
        return log.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }
}
