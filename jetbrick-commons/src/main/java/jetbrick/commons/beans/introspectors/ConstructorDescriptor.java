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
package jetbrick.commons.beans.introspectors;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import jetbrick.commons.beans.TypeResolverUtils;
import jetbrick.lang.ArrayUtils;

public class ConstructorDescriptor {
    private final ClassDescriptor classDescriptor;
    private final Constructor<?> constructor;
    private final Class<?>[] rawParameterTypes;

    public ConstructorDescriptor(ClassDescriptor classDescriptor, Constructor<?> constructor) {
        this.classDescriptor = classDescriptor;
        this.constructor = constructor;
        this.rawParameterTypes = doGetRawParameterTypes();
        constructor.setAccessible(true);
    }

    private Class<?>[] doGetRawParameterTypes() {
        Type[] params = constructor.getGenericParameterTypes();
        if (params.length == 0) {
            return ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        Class<?>[] rawParameterTypes = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            Type type = params[i];
            rawParameterTypes[i] = TypeResolverUtils.getRawType(type, classDescriptor.getType());
        }
        return rawParameterTypes;
    }

    public ClassDescriptor getClassDescriptor() {
        return classDescriptor;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public Class<?>[] getRawParameterTypes() {
        return rawParameterTypes;
    }

    public Class<?> getRawParameterComponentType(int parameterIndex, int componentIndex) {
        Type type = constructor.getGenericParameterTypes()[parameterIndex];
        return TypeResolverUtils.getComponentType(type, classDescriptor.getType(), componentIndex);
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return constructor.getAnnotation(annotationClass);
    }

    public Annotation[] getAnnotations() {
        return constructor.getAnnotations();
    }

    public Annotation[][] getRawParameterAnnotations() {
        return constructor.getParameterAnnotations();
    }

    public boolean isDefault() {
        return rawParameterTypes.length == 0;
    }

    public boolean isVarArgs() {
        return constructor.isVarArgs();
    }

    public boolean isFinal() {
        return Modifier.isFinal(constructor.getModifiers());
    }

    public boolean isStatic() {
        return Modifier.isStatic(constructor.getModifiers());
    }

    public boolean isPrivate() {
        return Modifier.isPrivate(constructor.getModifiers());
    }

    public boolean isPublic() {
        return Modifier.isPublic(constructor.getModifiers());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(classDescriptor.getType().getName()).append("#<init>(");
        for (int i = 0; i < rawParameterTypes.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            Class<?> type = rawParameterTypes[i];
            sb.append(type == null ? "<null>" : type.getSimpleName());
        }
        sb.append(')');
        return sb.toString();
    }
}
