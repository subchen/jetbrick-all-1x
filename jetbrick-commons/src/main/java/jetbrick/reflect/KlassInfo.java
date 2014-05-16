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
package jetbrick.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import jetbrick.reflect.Filters.FieldFilter;
import jetbrick.reflect.Filters.MethodFilter;
import jetbrick.reflect.asm.*;

public final class KlassInfo {
    private static final ConcurrentHashMap<Class<?>, KlassInfo> pool = new ConcurrentHashMap<Class<?>, KlassInfo>(128);

    public static KlassInfo create(final Class<?> type) {
        KlassInfo klass = pool.get(type);
        if (klass == null) {
            klass = new KlassInfo(type);
            KlassInfo old = pool.putIfAbsent(type, klass);
            if (old != null) {
                klass = old;
            }
        }
        return klass;
    }

    private final Class<?> type;

    private KlassInfo(Class<?> type) {
        this.type = type;
    }

    public String getName() {
        return type.getName();
    }

    public Class<?> getType() {
        return type;
    }

    public KlassInfo getSuperKlass() {
        Class<?> superKlass = type.getSuperclass();
        return (superKlass == null) ? null : KlassInfo.create(superKlass);
    }

    public List<KlassInfo> getInterfaces() {
        Class<?>[] interfaces = type.getInterfaces();
        if (interfaces.length == 0) {
            return Collections.emptyList();
        }
        List<KlassInfo> results = new ArrayList<KlassInfo>(interfaces.length);
        for (Class<?> intf : interfaces) {
            results.add(KlassInfo.create(intf));
        }
        return results;
    }

    // ------------------------------------------------------------------
    public Annotation[] getAnnotations() {
        return type.getAnnotations();
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return type.getAnnotation(annotationClass);
    }

    public <T extends Annotation> boolean isAnnotationPresent(Class<T> annotationClass) {
        return type.isAnnotationPresent(annotationClass);
    }

    // ------------------------------------------------------------------
    public int getModifiers() {
        return type.getModifiers();
    }

    public boolean isInterface() {
        return Modifier.isInterface(getModifiers());
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(getModifiers());
    }

    public boolean isStatic() {
        return Modifier.isStatic(getModifiers());
    }

    public boolean isPrivate() {
        return Modifier.isPrivate(getModifiers());
    }

    public boolean isProtected() {
        return Modifier.isProtected(getModifiers());
    }

    public boolean isPublic() {
        return Modifier.isPublic(getModifiers());
    }

    // ------------------------------------------------------------------
    private List<ConstructorInfo> declaredConstructors;

    public List<ConstructorInfo> getDeclaredConstructors() {
        if (declaredConstructors == null) {
            synchronized (this) {
                if (declaredConstructors == null) {
                    Constructor<?>[] constructors = type.getDeclaredConstructors();
                    if (constructors.length == 0) {
                        declaredConstructors = Collections.emptyList();
                    } else {
                        List<ConstructorInfo> results = new ArrayList<ConstructorInfo>(constructors.length);
                        for (int i = 0; i < constructors.length; i++) {
                            results.add(new ConstructorInfo(this, constructors[i], i));
                        }
                        declaredConstructors = Collections.unmodifiableList(results);
                    }
                }
            }
        }
        return declaredConstructors;
    }

    public ConstructorInfo getDeclaredConstructor(Class<?>... parameterTypes) {
        return searchExecutable(getDeclaredConstructors(), "<init>", parameterTypes);
    }

    public ConstructorInfo getDeclaredConstructor(Constructor<?> constructor) {
        for (ConstructorInfo info : getDeclaredConstructors()) {
            if (info.getConstructor() == constructor) {
                return info;
            }
        }
        return null;
    }

    public ConstructorInfo getDefaultConstructor() {
        for (ConstructorInfo info : getDeclaredConstructors()) {
            if (info.isDefault()) {
                return info;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    private List<MethodInfo> declaredMethods;

    public List<MethodInfo> getDeclaredMethods() {
        if (declaredMethods == null) {
            synchronized (this) {
                if (declaredMethods == null) {
                    Method[] methods = type.getDeclaredMethods();
                    if (methods.length == 0) {
                        declaredMethods = Collections.emptyList();
                    } else {
                        List<MethodInfo> results = new ArrayList<MethodInfo>(methods.length);
                        for (int i = 0; i < methods.length; i++) {
                            results.add(new MethodInfo(this, methods[i], i));
                        }
                        declaredMethods = Collections.unmodifiableList(results);
                    }
                }
            }
        }
        return declaredMethods;
    }

    public List<MethodInfo> getDeclaredMethods(MethodFilter filter) {
        List<MethodInfo> methods = getDeclaredMethods();
        if (methods.size() == 0) {
            return methods;
        }
        List<MethodInfo> results = new ArrayList<MethodInfo>(methods.size());
        for (MethodInfo method : methods) {
            if (filter.accept(method)) {
                results.add(method);
            }
        }
        return results;
    }

    public MethodInfo getDeclaredMethod(String name, Class<?>... parameterTypes) {
        return searchExecutable(getDeclaredMethods(), name, parameterTypes);
    }

    public MethodInfo getDeclaredMethod(Method method) {
        for (MethodInfo info : getDeclaredMethods()) {
            if (info.getMethod() == method) {
                return info;
            }
        }
        return null;
    }

    private List<MethodInfo> methods;

    public List<MethodInfo> getMethods() {
        if (methods == null) {
            synchronized (this) {
                if (methods == null) {
                    List<MethodInfo> results = new ArrayList<MethodInfo>();
                    KlassInfo klass = this;
                    while (klass != null) {
                        results.addAll(klass.getDeclaredMethods());
                        klass = klass.getSuperKlass();
                    }
                    methods = Collections.unmodifiableList(results);
                }
            }
        }
        return methods;
    }

    public List<MethodInfo> getMethods(MethodFilter filter) {
        List<MethodInfo> methods = getMethods();
        if (methods.size() == 0) {
            return methods;
        }
        List<MethodInfo> results = new ArrayList<MethodInfo>(methods.size());
        for (MethodInfo method : methods) {
            if (filter.accept(method)) {
                results.add(method);
            }
        }
        return results;
    }

    public MethodInfo getMethod(String name, Class<?>... parameterTypes) {
        return searchExecutable(getMethods(), name, parameterTypes);
    }

    private <T extends Executable> T searchExecutable(List<T> list, String name, Class<?>... parameterTypes) {
        for (T info : list) {
            if (info.getName().equals(name)) {
                Class<?>[] types = info.getParameterTypes();
                if (parameterTypes.length == types.length) {
                    boolean match = true;
                    for (int i = 0; i < parameterTypes.length; i++) {
                        if (types[i] != parameterTypes[i]) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        return info;
                    }
                }
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    private List<FieldInfo> declaredFields;

    public List<FieldInfo> getDeclaredFields() {
        if (declaredFields == null) {
            synchronized (this) {
                if (declaredFields == null) {
                    Field[] fields = type.getDeclaredFields();
                    if (fields.length == 0) {
                        declaredFields = Collections.emptyList();
                    } else {
                        List<FieldInfo> results = new ArrayList<FieldInfo>(fields.length);
                        for (int i = 0; i < fields.length; i++) {
                            results.add(new FieldInfo(this, fields[i], i));
                        }
                        declaredFields = Collections.unmodifiableList(results);
                    }
                }
            }
        }
        return declaredFields;
    }

    public List<FieldInfo> getDeclaredFields(FieldFilter filter) {
        List<FieldInfo> fields = getDeclaredFields();
        if (fields.size() == 0) {
            return fields;
        }
        List<FieldInfo> results = new ArrayList<FieldInfo>(fields.size());
        for (FieldInfo field : fields) {
            if (filter.accept(field)) {
                results.add(field);
            }
        }
        return results;
    }

    public FieldInfo getDeclaredField(String name) {
        for (FieldInfo field : getDeclaredFields()) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    public FieldInfo getDeclaredField(Field field) {
        for (FieldInfo info : getDeclaredFields()) {
            if (info.getField() == field) {
                return info;
            }
        }
        return null;
    }

    private List<FieldInfo> fields;

    public List<FieldInfo> getFields() {
        if (fields == null) {
            synchronized (this) {
                if (fields == null) {
                    List<FieldInfo> results = new ArrayList<FieldInfo>();
                    KlassInfo klass = this;
                    while (klass != null) {
                        results.addAll(klass.getDeclaredFields());
                        klass = klass.getSuperKlass();
                    }
                    fields = Collections.unmodifiableList(results);
                }
            }
        }
        return fields;
    }

    public List<FieldInfo> getFields(FieldFilter filter) {
        List<FieldInfo> fields = getFields();
        if (fields.size() == 0) {
            return fields;
        }
        List<FieldInfo> results = new ArrayList<FieldInfo>(fields.size());
        for (FieldInfo field : fields) {
            if (filter.accept(field)) {
                results.add(field);
            }
        }
        return results;
    }

    public FieldInfo getField(String name) {
        for (FieldInfo field : getFields()) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    private ASMConstructorAccessor constructorAccessor;
    private ASMMethodAccessor methodAccessor;
    private ASMFieldAccessor fieldAccessor;

    public ASMConstructorAccessor getConstructorAccessor() {
        if (constructorAccessor == null) {
            constructorAccessor = ASMFactory.generateConstructorAccessor(this);
        }
        return constructorAccessor;
    }

    public ASMMethodAccessor getMethodAccessor() {
        if (methodAccessor == null) {
            methodAccessor = ASMFactory.generateMethodAccessor(this);
        }
        return methodAccessor;
    }

    public ASMFieldAccessor getFieldAccessor() {
        if (fieldAccessor == null) {
            fieldAccessor = ASMFactory.generateFieldAccessor(this);
        }
        return fieldAccessor;
    }

    public Object newInstance() {
        if (ASMFactory.IS_ASM_ENABLED) {
            return getConstructorAccessor().newInstance();
        } else {
            try {
                return type.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // ------------------------------------------------------------------
    @Override
    public String toString() {
        return type.toString();
    }
}
