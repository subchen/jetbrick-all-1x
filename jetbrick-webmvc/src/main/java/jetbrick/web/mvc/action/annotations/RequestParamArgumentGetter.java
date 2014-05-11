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
import jetbrick.commons.lang.ArrayUtils;
import jetbrick.commons.lang.StringUtils;
import jetbrick.commons.typecast.Convertor;
import jetbrick.ioc.annotations.Managed;
import jetbrick.web.mvc.RequestContext;
import jetbrick.web.mvc.action.ArgumentGetterResolver;
import jetbrick.web.mvc.multipart.FilePart;

@Managed
public class RequestParamArgumentGetter implements AnnotatedArgumentGetter<RequestParam, Object> {
    private enum ObjectType {
        FILE, ARRAY, ELEMENT
    }

    private Class<?> type;
    private ObjectType objType;
    private String name;
    private boolean required;
    private String defaultValue;
    private Convertor<?> typeConvertor;

    @Override
    public void initialize(Class<?> type, RequestParam annotation) {
        this.type = type;

        if (FilePart.class.isAssignableFrom(type)) {
            objType = ObjectType.FILE;
            typeConvertor = null;
        } else if (type.isArray()) {
            objType = ObjectType.ARRAY;
            typeConvertor = ArgumentGetterResolver.getTypeConvertor(type.getComponentType());
        } else {
            objType = ObjectType.ELEMENT;
            typeConvertor = ArgumentGetterResolver.getTypeConvertor(type);
        }

        name = annotation.value();
        required = annotation.required();
        defaultValue = ValueConstants.defaultIfNull(annotation.defaultValue());
    }

    @Override
    public Object get(RequestContext ctx) {
        if (objType == ObjectType.ELEMENT) {
            String value = ctx.getParameter(name);
            if (value == null) {
                value = defaultValue;
            }
            if (value == null) {
                if (required) {
                    throw new IllegalStateException();
                }
                return null;
            }
            if (typeConvertor != null) {
                return typeConvertor.convert(value);
            }
            return value;
        } else if (objType == ObjectType.ARRAY) {
            String[] values = ctx.getParameterValues(name);
            if (values == null) {
                if (!ValueConstants.isEmpty(defaultValue)) {
                    values = StringUtils.split(defaultValue, ',');
                }
            }
            if (values == null) {
                values = ArrayUtils.EMPTY_STRING_ARRAY;
            }
            if (typeConvertor != null) {
                Object[] result = (Object[]) Array.newInstance(type.getComponentType(), values.length);
                for (int i = 0; i < values.length; i++) {
                    result[i] = typeConvertor.convert(values[i]);
                }
                return result;
            }
            return values;
        } else if (objType == ObjectType.FILE) {
            Object value = ctx.getFilePart(name);
            if (value == null && required) {
                throw new IllegalStateException();
            }
            return value;
        }
        throw new IllegalStateException();
    }
}
