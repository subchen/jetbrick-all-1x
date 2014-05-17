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
package jetbrick.web.mvc.action;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import jetbrick.commons.beans.introspectors.MethodDescriptor;
import jetbrick.ioc.Ioc;
import jetbrick.ioc.annotations.ManagedWith;
import jetbrick.lang.ArrayUtils;
import jetbrick.web.mvc.RequestContext;
import jetbrick.web.mvc.action.annotations.AnnotatedArgumentGetter;
import jetbrick.web.mvc.action.annotations.ArgumentGetter;
import jetbrick.web.mvc.config.WebConfig;

final class MethodInjector {
    private final Method method;
    private final ArgumentGetter<?>[] resolvers;

    @SuppressWarnings("unchecked")
    public static MethodInjector create(MethodDescriptor md) throws Exception {
        Class<?>[] parameterTypes = md.getRawParameterTypes();
        if (parameterTypes.length == 0) {
            return new MethodInjector(md.getMethod(), ArgumentGetter.EMPTY_ARRAY);
        }

        Ioc ioc = WebConfig.getInstance().getIoc();
        ArgumentGetterResolver resolver = WebConfig.getInstance().getArgumentGetterResolver();

        ArgumentGetter<?>[] resolvers = new ArgumentGetter[parameterTypes.length];
        Annotation[][] parameterAnnotations = md.getRawParameterAnnotations();
        for (int i = 0; i < resolvers.length; i++) {
            Class<?> type = parameterTypes[i];

            ArgumentGetter<?> getter = null;
            for (Annotation annotation : parameterAnnotations[i]) {
                Class<?> argumentGetterClass = resolver.lookup(annotation);
                if (argumentGetterClass == null) {
                    // 没有注册 annotation， 那么尝试自动发现
                    Class<? extends Annotation> annotationType = annotation.annotationType();
                    ManagedWith managedWith = annotationType.getAnnotation(ManagedWith.class);
                    if (managedWith != null) {
                        argumentGetterClass = managedWith.value();
                        resolver.register(annotationType, argumentGetterClass);
                    }
                }
                if (argumentGetterClass != null) {
                    getter = (ArgumentGetter<?>) ioc.injectClass(argumentGetterClass);
                    ((AnnotatedArgumentGetter<Annotation, ?>) getter).initialize(type, annotation);
                    break;
                }
            }
            if (getter == null) {
                // 没有找到标注，那么尝试根据参数类型来查找
                getter = resolver.lookup(type);
            }
            if (getter == null) {
                throw new IllegalStateException();
            }
            resolvers[i] = getter;
        }

        return new MethodInjector(md.getMethod(), resolvers);
    }

    public MethodInjector(Method method, ArgumentGetter<?>[] resolvers) {
        this.method = method;
        this.resolvers = resolvers;
    }

    public Object invoke(Object action, RequestContext ctx) throws Exception {
        Object[] parameters = ArrayUtils.EMPTY_OBJECT_ARRAY;
        int length = resolvers.length;
        if (length > 0) {
            parameters = new Object[length];
            for (int i = 0; i < length; i++) {
                parameters[i] = resolvers[i].get(ctx);
            }
        }
        return method.invoke(action, parameters);
    }
}
