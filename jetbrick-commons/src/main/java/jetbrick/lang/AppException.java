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

import java.util.HashMap;
import java.util.Map;

public class AppException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private static final String lineSeparator = System.getProperty("line.separator");

    /**
     * 代表了一个异常代码，给 AppException 用，子类应该是一个 enum.
     */
    public static interface ErrorCode {
    }

    public static AppException unchecked(Throwable e) {
        return unchecked(e, null);
    }

    public static AppException unchecked(Throwable e, ErrorCode errorCode) {
        if (e instanceof AppException) {
            AppException ex = (AppException) e;
            if (errorCode != null && errorCode != ex.getErrorCode()) {
                return new AppException(e, errorCode);
            }
            return ex;
        } else {
            return new AppException(e, errorCode);
        }
    }

    private ErrorCode errorCode;
    private Map<String, Object> props;

    public AppException() {
        super();
    }

    public AppException(String message) {
        super(message);
    }

    public AppException(Throwable cause) {
        super(cause);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }

    public AppException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AppException(Throwable cause, ErrorCode errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }

    public AppException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public AppException set(String name, Object value) {
        if (props == null) {
            props = new HashMap<String, Object>();
        }
        props.put(name, value);
        return this;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getSimpleMessage() {
        return super.getMessage();
    }

    @Override
    public String getMessage() {
        // quick path
        if (errorCode == null && props == null) {
            return getSimpleMessage();
        }

        // slow path
        StringBuilder sb = new StringBuilder();
        String message = getSimpleMessage();
        if (message != null) {
            sb.append(message);
        }
        if (errorCode != null) {
            if (sb.length() > 0) {
                sb.append(lineSeparator);
                sb.append("    ");
            }
            sb.append(errorCode.getClass().getSimpleName());
            sb.append('.');
            sb.append(errorCode.toString());
        }
        if (props != null) {
            for (Map.Entry<String, Object> entry : props.entrySet()) {
                if (sb.length() > 0) {
                    sb.append(lineSeparator);
                    sb.append("    ");
                }
                Object value = entry.getValue();
                if (value != null && value.getClass().isArray()) {
                    value = ArrayUtils.toString(value);
                }
                sb.append(entry.getKey());
                sb.append(" = [");
                sb.append(value);
                sb.append(']');
            }
        }
        return sb.toString();
    }
}
