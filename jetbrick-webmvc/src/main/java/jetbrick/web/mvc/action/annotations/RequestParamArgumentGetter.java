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
package jetbrick.web.mvc.action.annotations;

import java.lang.reflect.Array;
import jetbrick.ioc.annotations.Managed;
import jetbrick.lang.ArrayUtils;
import jetbrick.lang.StringUtils;
import jetbrick.lang.annotations.ValueConstants;
import jetbrick.typecast.Convertor;
import jetbrick.web.mvc.RequestContext;
import jetbrick.web.mvc.multipart.FilePart;

@Managed
public class RequestParamArgumentGetter implements AnnotatedArgumentGetter<RequestParam, Object> {
    // 区分不同的场景
    private enum Scenario {
        FILE, ARRAY, ELEMENT
    }

    private Scenario scenario;
    private Class<?> type; // 参数类型
    private String name;
    private boolean required;
    private String defaultValue;
    private Convertor<?> cast;

    @Override
    public void initialize(ArgumentContext<RequestParam> ctx) {
        type = ctx.getRawParameterType();
        if (FilePart.class.isAssignableFrom(type)) {
            scenario = Scenario.FILE;
            cast = null;
        } else if (type.isArray()) {
            scenario = Scenario.ARRAY;
            type = type.getComponentType();
            cast = ctx.getComponentTypeConvertor();
        } else {
            scenario = Scenario.ELEMENT;
            cast = ctx.getTypeConvertor();
        }

        RequestParam annotation = ctx.getAnnotation();
        name = annotation.value();
        if (ValueConstants.isEmptyOrNull(name)) {
            name = ctx.getParameterName();
        }
        required = annotation.required();
        defaultValue = ValueConstants.trimToNull(annotation.defaultValue());
    }

    @Override
    public Object get(RequestContext ctx) {
        switch (scenario) {
        case ELEMENT: {
            String value = ctx.getParameter(name);
            if (value == null) {
                value = defaultValue;
            }
            if (value == null) {
                if (required) {
                    throw new IllegalStateException("request parameter is not found: " + name);
                }
                return null;
            }
            if (cast != null) {
                return cast.convert(value);
            }
            return value;
        }
        case ARRAY: {
            String[] values = ctx.getParameterValues(name);
            if (values == null) {
                if (defaultValue != null) {
                    values = StringUtils.split(defaultValue, ',');
                }
            }
            if (values == null) {
                values = ArrayUtils.EMPTY_STRING_ARRAY;
            }
            if (cast != null) {
                Object[] result = (Object[]) Array.newInstance(type, values.length);
                for (int i = 0; i < values.length; i++) {
                    result[i] = cast.convert(values[i]);
                }
                return result;
            }
            return values;
        }
        case FILE: {
            Object value = ctx.getFilePart(name);
            if (value == null && required) {
                throw new IllegalStateException("upload file object is not found: " + name);
            }
            return value;
        }
        }

        throw new IllegalStateException("unreachable");
    }
}
