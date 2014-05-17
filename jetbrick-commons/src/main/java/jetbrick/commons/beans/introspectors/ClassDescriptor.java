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

import java.lang.reflect.*;
import java.util.*;
import jetbrick.collections.SoftHashMap;
import jetbrick.collections.multimap.MultiValueHashMap;
import jetbrick.collections.multimap.MultiValueMap;
import jetbrick.commons.beans.MethodLookupUtils;

public class ClassDescriptor {
    private static final Map<Class<?>, ClassDescriptor> beanCache = new SoftHashMap<Class<?>, ClassDescriptor>();
    private final Class<?> beanClass;
    private Map<String, FieldDescriptor> fieldMap;
    private List<FieldDescriptor> fieldList;
    private MultiValueMap<String, MethodDescriptor> methodMap;
    private List<MethodDescriptor> methodList;
    private List<ConstructorDescriptor> constructorMap;
    private Map<String, PropertyDescriptor> propertyMap;
    private List<PropertyDescriptor> propertyList;

    public static ClassDescriptor lookup(Class<?> beanClass) {
        ClassDescriptor descriptor = beanCache.get(beanClass);
        if (descriptor == null) {
            descriptor = new ClassDescriptor(beanClass);
            beanCache.put(beanClass, descriptor);
        }
        return descriptor;
    }

    private ClassDescriptor(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public Class<?> getType() {
        return beanClass;
    }

    //------------------------------------------------------------
    private Map<String, FieldDescriptor> getFieldMap() {
        if (fieldMap == null) {
            synchronized (this) {
                if (fieldMap == null) {
                    fieldMap = doGetFieldMap();
                }
            }
        }
        return fieldMap;
    }

    private Map<String, FieldDescriptor> doGetFieldMap() {
        Map<String, FieldDescriptor> fields = new HashMap<String, FieldDescriptor>();
        Class<?> klass = beanClass;
        while (klass != Object.class) {
            for (Field field : klass.getDeclaredFields()) {
                if (klass == beanClass || !Modifier.isPrivate(field.getModifiers())) {
                    if (!fields.containsKey(field.getName())) {
                        fields.put(field.getName(), new FieldDescriptor(this, field));
                    }
                }
            }
            klass = klass.getSuperclass();
        }
        return fields;
    }

    public List<FieldDescriptor> getFieldDescriptors() {
        if (fieldList == null) {
            fieldList = new ArrayList<FieldDescriptor>(getFieldMap().values());
        }
        return fieldList;
    }

    public FieldDescriptor getFieldDescriptor(String name) {
        return getFieldMap().get(name);
    }

    public FieldDescriptor getFieldDescriptor(Field field) {
        if (field.getDeclaringClass().isAssignableFrom(beanClass)) {
            return new FieldDescriptor(this, field);
        }
        throw new IllegalArgumentException("The field is not in this class " + field);
    }

    //------------------------------------------------------------
    private MultiValueMap<String, MethodDescriptor> getMethodMap() {
        if (methodMap == null) {
            synchronized (this) {
                if (methodMap == null) {
                    methodMap = doGetMethodMap();
                }
            }
        }
        return methodMap;
    }

    private MultiValueMap<String, MethodDescriptor> doGetMethodMap() {
        MultiValueMap<String, MethodDescriptor> methods = new MultiValueHashMap<String, MethodDescriptor>();
        Class<?> klass = beanClass;
        while (klass != Object.class) {
            for (Method method : klass.getDeclaredMethods()) {
                if (klass == beanClass || !Modifier.isPrivate(method.getModifiers())) {
                    methods.put(method.getName(), new MethodDescriptor(this, method));
                }
            }
            klass = klass.getSuperclass();
        }
        return methods;
    }

    public List<MethodDescriptor> getMethodDescriptors() {
        if (methodList == null) {
            methodList = new ArrayList<MethodDescriptor>(getMethodMap().values());
        }
        return methodList;
    }

    public List<MethodDescriptor> getMethodDescriptors(String name) {
        List<MethodDescriptor> methodDescriptors = getMethodMap().getList(name);
        if (methodDescriptors == null) {
            return Collections.emptyList();
        } else {
            return methodDescriptors;
        }
    }

    public MethodDescriptor getMethodDescriptor(String name, Class<?>... parameterTypes) {
        List<MethodDescriptor> methodDescriptors = getMethodMap().getList(name);
        if (methodDescriptors == null) {
            return null;
        }
        return MethodLookupUtils.lookupBestMethodDescriptor(getMethodDescriptors(), name, parameterTypes);
    }

    public MethodDescriptor getMethodDescriptor(Method method) {
        if (method.getDeclaringClass().isAssignableFrom(beanClass)) {
            return new MethodDescriptor(this, method);
        }
        throw new IllegalArgumentException("The method is not in this class " + method);
    }

    //------------------------------------------------------------
    private List<ConstructorDescriptor> getConstructorList() {
        if (constructorMap == null) {
            synchronized (this) {
                if (constructorMap == null) {
                    constructorMap = doGetConstructorList();
                }
            }
        }
        return constructorMap;
    }

    private List<ConstructorDescriptor> doGetConstructorList() {
        List<ConstructorDescriptor> constructors = new ArrayList<ConstructorDescriptor>(8);
        for (Constructor<?> constructor : beanClass.getDeclaredConstructors()) {
            constructors.add(new ConstructorDescriptor(this, constructor));
        }
        return constructors;
    }

    public List<ConstructorDescriptor> getConstructorDescriptors() {
        return getConstructorList();
    }

    public ConstructorDescriptor getDefaultConstructorDescriptor() {
        for (ConstructorDescriptor descriptor : getConstructorList()) {
            if (descriptor.isDefault()) {
                return descriptor;
            }
        }
        return null;
    }

    public ConstructorDescriptor getConstructorDescriptor(Class<?>... parameterTypes) {
        return MethodLookupUtils.lookupBestConstructorDescriptor(getConstructorDescriptors(), parameterTypes);
    }

    //------------------------------------------------------------
    private Map<String, PropertyDescriptor> getPropertyMap() {
        if (propertyMap == null) {
            synchronized (this) {
                if (propertyMap == null) {
                    propertyMap = doGetPropertyMap();
                }
            }
        }
        return propertyMap;
    }

    private Map<String, PropertyDescriptor> doGetPropertyMap() {
        Map<String, PropertyDescriptor> map = new HashMap<String, PropertyDescriptor>();
        for (FieldDescriptor descriptor : getFieldDescriptors()) {
            if (descriptor.isStatic()) continue;
            PropertyDescriptor pd = new PropertyDescriptor(this, descriptor);
            map.put(descriptor.getName(), pd);
        }
        for (MethodDescriptor descriptor : getMethodDescriptors()) {
            if (descriptor.isStatic()) continue;
            if (descriptor.isReadMethod()) {
                String name = descriptor.getPropertyName();
                PropertyDescriptor pd = map.get(name);
                if (pd == null) {
                    pd = new PropertyDescriptor(this, null);
                    map.put(name, pd);
                }
                pd.setReadMethodDescriptor(descriptor);
            } else if (descriptor.isWriteMethod()) {
                String name = descriptor.getPropertyName();
                PropertyDescriptor pd = map.get(name);
                if (pd == null) {
                    pd = new PropertyDescriptor(this, null);
                    map.put(name, pd);
                }
                pd.setWriteMethodDescriptor(descriptor);
            }
        }
        return map;
    }

    public List<PropertyDescriptor> getPropertyDescriptors() {
        if (propertyList == null) {
            propertyList = new ArrayList<PropertyDescriptor>(getPropertyMap().values());
        }
        return propertyList;
    }

    public PropertyDescriptor getPropertyDescriptor(String name) {
        return getPropertyMap().get(name);
    }

    @Override
    public String toString() {
        return beanClass.getName();
    }
}
