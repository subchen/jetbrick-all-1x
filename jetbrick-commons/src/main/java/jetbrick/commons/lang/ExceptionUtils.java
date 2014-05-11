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
package jetbrick.commons.lang;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.TreeMap;

public final class ExceptionUtils {

    public static RuntimeException unchecked(Throwable e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        if (e instanceof InvocationTargetException) {
            return unchecked(((InvocationTargetException) e).getTargetException());
        }
        return new RuntimeException(e);
    }

    @SuppressWarnings("serial")
    static class AppException extends RuntimeException {
        private final static String lineSeparator = System.getProperty("line.separator");
        private Object[] args;
        private final Map<String, Object> props = new TreeMap<String, Object>();

        public static AppException unchecked(Throwable e) {
            if (e instanceof AppException) {
                return (AppException) e;
            } else {
                return new AppException(e);
            }
        }

        public AppException(Throwable cause) {
            super(cause);
        }

        public AppException(Throwable cause, String message, Object... args) {
            super(message, cause);
            this.args = args;
        }

        public Map<String, Object> getProperties() {
            return props;
        }

        @SuppressWarnings("unchecked")
        public <T> T get(String name) {
            return (T) props.get(name);
        }

        public AppException set(String name, Object value) {
            props.put(name, value);
            return this;
        }

        public String getSimpleMessage() {
            String message = super.getMessage();
            if (message != null) {
                if (args != null && args.length > 0) {
                    message = String.format(message, args);
                }
            }
            return message;
        }

        @Override
        public String getMessage() {
            StringBuilder sb = new StringBuilder();
            String message = getSimpleMessage();
            if (message != null) {
                sb.append(message);
            }
            for (String key : props.keySet()) {
                if (sb.length() > 0) {
                    sb.append(lineSeparator);
                    sb.append('\t');
                }
                Object value = props.get(key);
                if (value != null && value.getClass().isArray()) {
                    value = ArrayUtils.toString(value);
                }
                sb.append(key);
                sb.append(" = [");
                sb.append(value);
                sb.append(']');
            }
            return sb.length() == 0 ? null : sb.toString();
        }
    }

}
