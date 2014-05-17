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
package jetbrick.ioc.objects;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import jetbrick.beans.introspectors.*;
import jetbrick.commons.config.Configuration;
import jetbrick.ioc.Ioc;
import jetbrick.ioc.annotations.*;
import jetbrick.ioc.injectors.*;
import jetbrick.lang.ExceptionUtils;

class IocObjectUtils {

    //---------------------------------------------------------------------------
    // @Inject 标注的构造函数
    public static CtorInjector doGetCtorInjector(Ioc ioc, ClassDescriptor meta) {
        ConstructorDescriptor ctor = null;

        // 找到对应的构造函数
        for (ConstructorDescriptor descriptor : meta.getConstructorDescriptors()) {
            Inject ref = descriptor.getAnnotation(Inject.class);
            if (ref != null) {
                if (ctor != null) {
                    throw new IllegalStateException("More than two constructors are annotated as injection points in bean: " + meta);
                }
                ctor = descriptor;
            }
        }
        if (ctor == null) {
            return null;
        }

        // 构造函数参数
        ParameterInjector[] parameters = ParameterInjector.EMPTY_ARRAY;
        Class<?>[] parameterTypes = ctor.getRawParameterTypes();
        Annotation[][] parameterAnnotations = ctor.getRawParameterAnnotations();
        if (parameterTypes.length > 0) {
            parameters = new ParameterInjector[parameterTypes.length];
            for (int i = 0; i < parameters.length; i++) {
                Class<?> parameterInjectorClass = DefaultParameterInjector.class;
                Annotation parameterAnnotation = null;
                // 查找 @Inject/@Config 等标注
                for (Annotation anno : parameterAnnotations[i]) {
                    InjectParameterWith with = anno.annotationType().getAnnotation(InjectParameterWith.class);
                    if (with != null) {
                        parameterInjectorClass = with.value();
                        parameterAnnotation = anno;
                        break;
                    }
                }
                try {
                    parameters[i] = (ParameterInjector) parameterInjectorClass.newInstance();
                    parameters[i].initialize(ioc, parameterTypes[i], parameterAnnotation);
                } catch (Exception e) {
                    throw ExceptionUtils.unchecked(e);
                }
            }
        }

        //
        return new CtorInjector(ctor.getConstructor(), parameters);
    }

    // @Inject/@Config 等标注的字段
    public static List<FieldInjector> doGetFieldInjectors(Ioc ioc, ClassDescriptor meta) {
        List<FieldInjector> injectors = new ArrayList<FieldInjector>(8);
        for (FieldDescriptor fd : meta.getFieldDescriptors()) {
            for (Annotation anno : fd.getAnnotations()) {
                InjectFieldWith with = anno.annotationType().getAnnotation(InjectFieldWith.class);
                if (with != null) {
                    try {
                        FieldInjector injector = with.value().newInstance();
                        injector.initialize(ioc, fd, anno);
                        injectors.add(injector);
                    } catch (Exception e) {
                        throw ExceptionUtils.unchecked(e);
                    }
                }
            }
        }
        if (injectors.size() == 0) {
            return Collections.emptyList();
        }
        return injectors;
    }

    // 注入配置文件中自定义的属性字段
    public static List<PropertyInjector> doGetPropertyInjectors(Ioc ioc, ClassDescriptor meta, Configuration properties) {
        if (properties == null || properties.size() == 0) {
            return Collections.emptyList();
        }
        List<PropertyInjector> injectors = new ArrayList<PropertyInjector>();
        for (String name : properties.keySet()) {
            PropertyDescriptor pd = meta.getPropertyDescriptor(name);
            if (pd == null) {
                throw new IllegalStateException("Property not found: " + meta.toString() + "#" + name);
            }
            if (!pd.writable()) {
                throw new IllegalStateException("Property not writable: " + pd);
            }

            Object value;
            if (List.class.isAssignableFrom(pd.getRawType())) {
                value = properties.getValueList(pd.getName(), pd.getRawComponentType(0));
            } else {
                value = properties.getValue(pd.getName(), pd.getRawType(), null);
            }
            injectors.add(new PropertyInjector(pd, value));
        }
        return injectors;
    }

    // @Initialize 标注的函数
    public static Method doGetInitializeMethod(ClassDescriptor meta) {
        MethodDescriptor found = null;
        for (MethodDescriptor md : meta.getMethodDescriptors()) {
            IocInit ref = md.getAnnotation(IocInit.class);
            if (ref != null) {
                if (found != null) {
                    throw new IllegalStateException("More than two methods are annotated @Initialize in bean: " + meta);
                }
                if (md.getRawParameterTypes().length != 0) {
                    throw new IllegalStateException("@Initialize method parameters must be empty.");
                }
                found = md;
            }
        }
        return (found == null) ? null : found.getMethod();
    }
}
