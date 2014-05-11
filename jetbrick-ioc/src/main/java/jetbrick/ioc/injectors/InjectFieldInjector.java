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
package jetbrick.ioc.injectors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import jetbrick.commons.beans.introspectors.FieldDescriptor;
import jetbrick.commons.lang.Validate;
import jetbrick.ioc.Ioc;
import jetbrick.ioc.annotations.Inject;
import jetbrick.ioc.annotations.ValueConstants;

//注入 @Inject 标注的字段
public class InjectFieldInjector implements FieldInjector {
    private Ioc ioc;
    private String name;
    private Field field;
    private boolean required;

    @Override
    public void initialize(Ioc ioc, FieldDescriptor fd, Annotation anno) {
        Validate.isInstanceOf(Inject.class, anno);

        Inject inject = (Inject) anno;
        this.ioc = ioc;
        this.field = fd.getField();
        this.name = ValueConstants.defaultValue(inject.value(), fd.getRawType().getName());
        this.required = inject.required();
    }

    @Override
    public void set(Object object) throws Exception {
        Object value = ioc.getBean(name);
        if (value == null && required) {
            throw new IllegalStateException("Can't inject bean: " + name + " for field: " + object.getClass().getName() + '#' + field.getName());
        }
        field.set(object, value);
    }
}
