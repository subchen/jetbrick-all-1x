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
import jetbrick.lang.concurrent.ConcurrentInitializer;
import jetbrick.lang.concurrent.LazyInitializer;
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
    private ConcurrentInitializer<List<ConstructorInfo>> declaredConstructorsGet = new LazyInitializer<List<ConstructorInfo>>() {
        @Override
        protected List<ConstructorInfo> initialize() {
            Constructor<?>[] constructors = type.getDeclaredConstructors();
            if (constructors.length == 0) {
                return Collections.emptyList();
            } else {
                List<ConstructorInfo> results = new ArrayList<ConstructorInfo>(constructors.length);
                for (int i = 0; i < constructors.length; i++) {
                    results.add(new ConstructorInfo(KlassInfo.this, constructors[i], i));
                }
                return Collections.unmodifiableList(results);
            }
        }
    };

    public List<ConstructorInfo> getDeclaredConstructors() {
        return declaredConstructorsGet.get();
    }

    public ConstructorInfo getDeclaredConstructor(Class<?>... parameterTypes) {
        return searchExecutable(declaredConstructorsGet.get(), "<init>", parameterTypes);
    }

    public ConstructorInfo getDeclaredConstructor(Constructor<?> constructor) {
        for (ConstructorInfo info : declaredConstructorsGet.get()) {
            if (info.getConstructor() == constructor) {
                return info;
            }
        }
        return null;
    }

    public ConstructorInfo getDefaultConstructor() {
        for (ConstructorInfo info : declaredConstructorsGet.get()) {
            if (info.isDefault()) {
                return info;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    private ConcurrentInitializer<List<MethodInfo>> declaredMethodsGet = new LazyInitializer<List<MethodInfo>>() {
        @Override
        protected List<MethodInfo> initialize() {
            Method[] methods = type.getDeclaredMethods();
            if (methods.length == 0) {
                return Collections.emptyList();
            } else {
                List<MethodInfo> results = new ArrayList<MethodInfo>(methods.length);
                for (int i = 0; i < methods.length; i++) {
                    results.add(new MethodInfo(KlassInfo.this, methods[i], i));
                }
                return Collections.unmodifiableList(results);
            }
        }
    };

    public List<MethodInfo> getDeclaredMethods() {
        return declaredMethodsGet.get();
    }

    public List<MethodInfo> getDeclaredMethods(MethodFilter filter) {
        List<MethodInfo> methods = declaredMethodsGet.get();
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
        return searchExecutable(declaredMethodsGet.get(), name, parameterTypes);
    }

    public MethodInfo getDeclaredMethod(Method method) {
        for (MethodInfo info : declaredMethodsGet.get()) {
            if (info.getMethod() == method) {
                return info;
            }
        }
        return null;
    }

    private ConcurrentInitializer<List<MethodInfo>> methodsGet = new LazyInitializer<List<MethodInfo>>() {
        @Override
        protected List<MethodInfo> initialize() {
            List<MethodInfo> results = new ArrayList<MethodInfo>();
            KlassInfo klass = KlassInfo.this;
            while (klass != null) {
                results.addAll(klass.getDeclaredMethods());
                klass = klass.getSuperKlass();
            }
            return Collections.unmodifiableList(results);
        }
    };

    public List<MethodInfo> getMethods() {
        return methodsGet.get();
    }

    public List<MethodInfo> getMethods(MethodFilter filter) {
        List<MethodInfo> methods = methodsGet.get();
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
        return searchExecutable(methodsGet.get(), name, parameterTypes);
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
    private ConcurrentInitializer<List<FieldInfo>> declaredFieldsGet = new LazyInitializer<List<FieldInfo>>() {
        @Override
        protected List<FieldInfo> initialize() {
            Field[] fields = type.getDeclaredFields();
            if (fields.length == 0) {
                return Collections.emptyList();
            }
            List<FieldInfo> results = new ArrayList<FieldInfo>(fields.length);
            for (int i = 0; i < fields.length; i++) {
                results.add(new FieldInfo(KlassInfo.this, fields[i], i));
            }
            return Collections.unmodifiableList(results);
        }
    };

    public List<FieldInfo> getDeclaredFields() {
        return declaredFieldsGet.get();
    }

    public List<FieldInfo> getDeclaredFields(FieldFilter filter) {
        List<FieldInfo> fields = declaredFieldsGet.get();
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
        for (FieldInfo field : declaredFieldsGet.get()) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    public FieldInfo getDeclaredField(Field field) {
        for (FieldInfo info : declaredFieldsGet.get()) {
            if (info.getField() == field) {
                return info;
            }
        }
        return null;
    }

    private ConcurrentInitializer<List<FieldInfo>> fieldsGet = new LazyInitializer<List<FieldInfo>>() {
        @Override
        protected List<FieldInfo> initialize() {
            List<FieldInfo> results = new ArrayList<FieldInfo>();
            KlassInfo klass = KlassInfo.this;
            while (klass != null) {
                results.addAll(klass.getDeclaredFields());
                klass = klass.getSuperKlass();
            }
            return Collections.unmodifiableList(results);
        }
    };

    public List<FieldInfo> getFields() {
        return fieldsGet.get();
    }

    public List<FieldInfo> getFields(FieldFilter filter) {
        List<FieldInfo> fields = fieldsGet.get();
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
        for (FieldInfo field : fieldsGet.get()) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    private ConcurrentInitializer<List<PropertyInfo>> propertiesGet = new LazyInitializer<List<PropertyInfo>>() {
        @Override
        protected List<PropertyInfo> initialize() {
            List<MethodInfo> methods = methodsGet.get();
            Map<String, PropertyInfo> map = new HashMap<String, PropertyInfo>(methods.size());
            for (MethodInfo method : methods) {
                if (method.isStatic()) continue;
                if (method.isAbstract()) continue;
                if (!method.isPublic()) continue;

                if (method.isReadMethod()) {
                    String name = method.getPropertyName();
                    PropertyInfo propertyInfo = map.get(name);
                    if (propertyInfo == null) {
                        propertyInfo = new PropertyInfo(KlassInfo.this, name);
                    }
                    propertyInfo.setGetter(method);
                }
                if (method.isWriteMethod()) {
                    String name = method.getPropertyName();
                    PropertyInfo propertyInfo = map.get(name);
                    if (propertyInfo == null) {
                        propertyInfo = new PropertyInfo(KlassInfo.this, name);
                    }
                    propertyInfo.setSetter(method);
                }
            }
            if (map.size() == 0) {
                return Collections.emptyList();
            }
            return Collections.unmodifiableList(new ArrayList<PropertyInfo>(map.values()));
        }
    };

    public List<PropertyInfo> getProperties() {
        return propertiesGet.get();
    }

    public PropertyInfo getProperty(String name) {
        for (PropertyInfo prop : propertiesGet.get()) {
            if (prop.getName().equals(name)) {
                return prop;
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
    public Map<String, Object> asBeanMap(final Object object) {
        return new Map<String, Object>() {

            @Override
            public int size() {
                return KlassInfo.this.getProperties().size();
            }

            @Override
            public boolean isEmpty() {
                return KlassInfo.this.getProperties().isEmpty();
            }

            @Override
            public boolean containsKey(Object key) {
                if (key instanceof String) {
                    return KlassInfo.this.getProperty((String) key) != null;
                }
                return false;
            }

            @Override
            public Object get(Object key) {
                if (key instanceof String) {
                    PropertyInfo prop = KlassInfo.this.getProperty((String) key);
                    if (prop != null) {
                        return prop.get(object);
                    }
                }
                return null;
            }

            @Override
            public Object put(String key, Object value) {
                PropertyInfo prop = KlassInfo.this.getProperty(key);
                if (prop != null) {
                    Object old = prop.get(object);
                    prop.set(object, value);
                    return old;
                }
                return null;
            }

            @Override
            public Set<java.util.Map.Entry<String, Object>> entrySet() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean containsValue(Object value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Object remove(Object key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void putAll(Map<? extends String, ? extends Object> m) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void clear() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Set<String> keySet() {
                Set<String> set = new HashSet<String>();
                for (PropertyInfo prop : KlassInfo.this.getProperties()) {
                    set.add(prop.getName());
                }
                return set;
            }

            @Override
            public Collection<Object> values() {
                throw new UnsupportedOperationException();
            }
        };
    }

    // ------------------------------------------------------------------
    @Override
    public String toString() {
        return type.toString();
    }
}
