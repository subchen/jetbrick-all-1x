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
import java.util.List;
import jetbrick.ioc.Ioc;
import jetbrick.ioc.annotations.Config;
import jetbrick.lang.Validate;
import jetbrick.lang.annotations.ValueConstants;
import jetbrick.reflect.FieldInfo;

// 注入 @Config 标注的字段
public class ConfigFieldInjector implements FieldInjector {
    private FieldInfo field;
    private boolean required;
    private Object value;

    @Override
    public void initialize(FieldContext ctx) {
        Annotation annotation = ctx.getAnnotation();
        Validate.isInstanceOf(Config.class, annotation);

        Config config = (Config) annotation;
        this.field = ctx.getField();
        this.required = config.required();

        // 类型转换
        Ioc ioc = ctx.getIoc();
        Class<?> clazz = ctx.getRawFieldType();
        if (List.class == clazz) {
            Class<?> elementType = ctx.getRawFieldComponentType(0);
            value = ioc.getConfigAsList(config.value(), elementType);
        } else if (clazz.isArray()) {
            Class<?> elementType = clazz.getComponentType();
            value = ioc.getConfigAsArray(config.value(), elementType);
        } else {
            String defaultValue = ValueConstants.trimToNull(config.defaultValue());
            value = ioc.getConfig(config.value(), clazz, defaultValue);
        }
    }

    @Override
    public void set(Object object) throws Exception {
        if (value == null && required) {
            throw new IllegalStateException("Can't inject field: " + field);
        }
        field.set(object, value);
    }
}
