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

public abstract class Logger {

    public abstract void debug(String message, Object... args);

    public abstract void debug(Object message);

    public abstract void info(String message, Object... args);

    public abstract void info(Object message);

    public abstract void warn(String message, Object... args);

    public abstract void warn(Throwable e);

    public abstract void warn(Throwable e, String message, Object... args);

    public abstract void error(String message, Object... args);

    public abstract void error(Throwable e);

    public abstract void error(Throwable e, String message, Object... args);

    public abstract boolean isDebugEnabled();

    public abstract boolean isInfoEnabled();

    public abstract boolean isWarnEnabled();

    public abstract boolean isErrorEnabled();

    protected String format(String message, Object... args) {
        if (args == null || args.length == 0) {
            return message;
        } else if (message.length() == 0) {
            return message;
        } else {
            StringBuilder sb = null;
            int index = 0;
            int start = 0;
            int end = message.indexOf("{}");
            while (end >= 0) {
                if (sb == null) {
                    sb = new StringBuilder();
                }
                sb.append(message, start, end);
                if (index < args.length) {
                    sb.append(args[index++]);
                } else {
                    sb.append("{}");
                    break;
                }
                start = end + 2;
                end = message.indexOf("{}", start);
            }
            if (start < message.length()) {
                if (sb == null) {
                    return message;
                }
                sb.append(message, start, message.length());
            }
            return sb.toString();
        }
    }

    protected String format(Object message) {
        return (message == null) ? "<null>" : message.toString();
    }
}
