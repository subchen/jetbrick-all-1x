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
package jetbrick.beans.introspectors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import jetbrick.beans.TypeResolverUtils;

public class FieldDescriptor {
    private final ClassDescriptor classDescriptor;
    private final Field field;
    private final Class<?> rawType;

    public FieldDescriptor(ClassDescriptor classDescriptor, Field field) {
        this.classDescriptor = classDescriptor;
        this.field = field;
        this.rawType = TypeResolverUtils.getRawType(field.getGenericType(), classDescriptor.getType());
        field.setAccessible(true);
    }

    public ClassDescriptor getClassDescriptor() {
        return classDescriptor;
    }

    public Field getField() {
        return field;
    }

    public String getName() {
        return field.getName();
    }

    public Class<?> getRawType() {
        return rawType;
    }

    public Class<?> getRawComponentType(int index) {
        return TypeResolverUtils.getComponentType(field.getGenericType(), classDescriptor.getType(), index);
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return field.getAnnotation(annotationClass);
    }

    public Annotation[] getAnnotations() {
        return field.getAnnotations();
    }

    public boolean isFinal() {
        return Modifier.isFinal(field.getModifiers());
    }

    public boolean isStatic() {
        return Modifier.isStatic(field.getModifiers());
    }

    public boolean isPrivate() {
        return Modifier.isPrivate(field.getModifiers());
    }

    public boolean isPublic() {
        return Modifier.isPublic(field.getModifiers());
    }

    public Object invokeGetter(Object bean) {
        try {
            return field.get(bean);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void invokeSetter(Object bean, Object value) {
        if (isFinal()) {
            throw new IllegalStateException("Field is not final: " + toString());
        }
        try {
            field.set(bean, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return classDescriptor.getType().getName() + '#' + field.getName();
    }
}
