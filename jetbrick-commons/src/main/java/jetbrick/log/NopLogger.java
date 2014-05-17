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

public class NopLogger extends Logger {
    @Override
    public void debug(String message, Object... args) {
    }

    @Override
    public void debug(Object message) {
    }

    @Override
    public void info(String message, Object... args) {
    }

    @Override
    public void info(Object message) {
    }

    @Override
    public void warn(String message, Object... args) {
    }

    @Override
    public void warn(Throwable e) {
    }

    @Override
    public void warn(Throwable e, String message, Object... args) {
    }

    @Override
    public void error(String message, Object... args) {
    }

    @Override
    public void error(Throwable e) {
    }

    @Override
    public void error(Throwable e, String message, Object... args) {
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }
}
