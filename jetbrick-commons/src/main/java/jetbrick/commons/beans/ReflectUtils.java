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
package jetbrick.commons.beans;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

public class ReflectUtils {

    public static List<Field> getAccessibleFields(Class<?> beanClass) {
        List<Field> fields = new ArrayList<Field>();
        Class<?> klass = beanClass;
        while (klass != Object.class) {
            for (Field field : klass.getDeclaredFields()) {
                if (klass == beanClass || !Modifier.isPrivate(field.getModifiers())) {
                    fields.add(field);
                }
            }
            klass = klass.getSuperclass();
        }
        return fields;
    }

    public static List<Field> getAccessibleFields(Class<?> beanClass, Class<? extends Annotation> annotationClass) {
        List<Field> fields = new ArrayList<Field>();
        Class<?> klass = beanClass;
        while (klass != Object.class) {
            for (Field field : klass.getDeclaredFields()) {
                if (klass == beanClass || !Modifier.isPrivate(field.getModifiers())) {
                    if (field.isAnnotationPresent(annotationClass)) {
                        fields.add(field);
                    }
                }
            }
            klass = klass.getSuperclass();
        }
        return fields;
    }

    public static List<Method> getAccessibleMethods(Class<?> beanClass) {
        List<Method> methods = new ArrayList<Method>();
        Class<?> klass = beanClass;
        while (klass != Object.class) {
            for (Method method : klass.getDeclaredMethods()) {
                if (klass == beanClass || !Modifier.isPrivate(method.getModifiers())) {
                    methods.add(method);
                }
            }
            klass = klass.getSuperclass();
        }
        return methods;
    }

    public static List<Method> getAccessibleMethods(Class<?> beanClass, Class<? extends Annotation> annotationClass) {
        List<Method> methods = new ArrayList<Method>();
        Class<?> klass = beanClass;
        while (klass != Object.class) {
            for (Method method : klass.getDeclaredMethods()) {
                if (klass == beanClass || !Modifier.isPrivate(method.getModifiers())) {
                    if (method.isAnnotationPresent(annotationClass)) {
                        methods.add(method);
                    }
                }
            }
            klass = klass.getSuperclass();
        }
        return methods;
    }
}
