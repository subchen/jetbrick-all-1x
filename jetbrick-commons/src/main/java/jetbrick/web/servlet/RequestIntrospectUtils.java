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
package jetbrick.web.servlet;

import java.util.*;
import javax.servlet.ServletRequest;
import jetbrick.commons.beans.introspectors.ClassDescriptor;
import jetbrick.commons.beans.introspectors.PropertyDescriptor;
import jetbrick.commons.lang.StringUtils;
import jetbrick.commons.typecast.TypeCastUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RequestIntrospectUtils {
    protected static final Logger log = LoggerFactory.getLogger(RequestIntrospectUtils.class);

    public static void introspect(Object form, ServletRequest request) {
        ClassDescriptor desc = ClassDescriptor.lookup(form.getClass());
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            String name = entry.getKey();
            try {
                PropertyDescriptor pd = desc.getPropertyDescriptor(name);
                if (pd == null) continue;

                String values[] = entry.getValue();
                if (values == null) continue;

                Class<?> type = pd.getRawType();
                if (type.isArray()) {
                    Class<?> componentType = type.getComponentType();
                    Object data = TypeCastUtils.convertToArray(values, componentType);
                    pd.invokeSetter(form, data);
                } else if (type == List.class || type == Collection.class) {
                    Class<?> componentType = pd.getRawComponentType(0);
                    Object data = TypeCastUtils.convertToList(values, componentType);
                    pd.invokeSetter(form, data);
                } else {
                    String value = StringUtils.trimToNull(values[0]);
                    if (value == null) {
                        if (type.isPrimitive()) {
                            continue;
                        } else {
                            pd.invokeSetter(form, null);
                        }
                    } else {
                        Object data = TypeCastUtils.convert(value, type);
                        pd.invokeSetter(form, data);
                    }
                }
            } catch (Throwable e) {
                log.warn("Can't set property for Object.", e);
            }
        }
    }

}
