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
import java.lang.reflect.*;
import jetbrick.beans.TypeResolverUtils;
import jetbrick.lang.ArrayUtils;
import jetbrick.lang.IdentifiedNameUtils;

public class MethodDescriptor {
    private final ClassDescriptor classDescriptor;
    private final Method method;
    private final Class<?> rawReturnType;
    private final Class<?>[] rawParameterTypes;

    public MethodDescriptor(ClassDescriptor classDescriptor, Method method) {
        this.classDescriptor = classDescriptor;
        this.method = method;
        this.rawReturnType = TypeResolverUtils.getRawType(method.getGenericReturnType(), classDescriptor.getType());
        this.rawParameterTypes = doGetRawParameterTypes();
        method.setAccessible(true);
    }

    private Class<?>[] doGetRawParameterTypes() {
        Type[] params = method.getGenericParameterTypes();
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

    public Method getMethod() {
        return method;
    }

    public Class<?> getRawReturnType() {
        return rawReturnType;
    }

    public Class<?> getRawReturnComponentType(int index) {
        return TypeResolverUtils.getComponentType(method.getGenericReturnType(), classDescriptor.getType(), index);
    }

    public Class<?>[] getRawParameterTypes() {
        return rawParameterTypes;
    }

    public Class<?> getRawParameterComponentType(int parameterIndex, int componentIndex) {
        Type type = method.getGenericParameterTypes()[parameterIndex];
        return TypeResolverUtils.getComponentType(type, classDescriptor.getType(), componentIndex);
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return method.getAnnotation(annotationClass);
    }

    public Annotation[] getAnnotations() {
        return method.getAnnotations();
    }

    public Annotation[][] getRawParameterAnnotations() {
        return method.getParameterAnnotations();
    }

    public String getName() {
        return method.getName();
    }

    public boolean isVarArgs() {
        return method.isVarArgs();
    }

    public boolean isFinal() {
        return Modifier.isFinal(method.getModifiers());
    }

    public boolean isStatic() {
        return Modifier.isStatic(method.getModifiers());
    }

    public boolean isPrivate() {
        return Modifier.isPrivate(method.getModifiers());
    }

    public boolean isPublic() {
        return Modifier.isPublic(method.getModifiers());
    }

    public boolean isReadMethod() {
        Class<?> resultType = method.getReturnType();
        if (rawParameterTypes.length == 0 && resultType != Void.TYPE) {
            String name = method.getName();
            if (name.length() > 3 && name.startsWith("get")) {
                return true;
            }
            if (name.length() > 2 && name.startsWith("is")) {
                return resultType == Boolean.TYPE || resultType == Boolean.class;
            }
        }
        return false;
    }

    public boolean isWriteMethod() {
        Class<?> resultType = method.getReturnType();
        if (rawParameterTypes.length == 1 && resultType == Void.TYPE) {
            String name = method.getName();
            if (name.length() > 3 && name.startsWith("set")) {
                return true;
            }
        }
        return false;
    }

    protected String getPropertyName() {
        String name = method.getName();
        if (name.startsWith("get")) {
            name = name.substring(3);
        } else if (name.startsWith("is")) {
            name = name.substring(2);
        } else if (name.startsWith("set")) {
            name = name.substring(3);
        }
        return IdentifiedNameUtils.decapitalize(name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(classDescriptor.getType().getName());
        sb.append('#').append(method.getName()).append('(');
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
