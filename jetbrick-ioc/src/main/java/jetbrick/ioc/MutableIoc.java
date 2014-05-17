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
package jetbrick.ioc;

import java.lang.reflect.Modifier;
import java.util.*;
import jetbrick.io.config.Configuration;
import jetbrick.ioc.loaders.IocConfiguration;
import jetbrick.ioc.loaders.IocLoader;
import jetbrick.ioc.objects.*;
import jetbrick.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MutableIoc implements Ioc {
    private final Logger log = LoggerFactory.getLogger(Ioc.class);
    private final Map<String, IocObject> pool = new HashMap<String, IocObject>();
    private final IocConfiguration config = new IocConfiguration(this);

    public void load(IocLoader loader) {
        loader.load(this);
    }

    public void addConfig(String name, String value) {
        config.put(name, value);
    }

    // 添加用户自定义的对象
    public void addBean(Object beanObject) {
        Validate.notNull(beanObject);
        addBean(beanObject.getClass().getName(), beanObject);
    }

    // 添加用户自定义的对象
    public void addBean(Class<?> beanClass, Object beanObject) {
        Validate.notNull(beanClass);
        addBean(beanClass.getName(), beanObject);
    }

    // 添加用户自定义的对象
    public void addBean(String name, Object beanObject) {
        Validate.notNull(beanObject);
        addBean(name, new ValueObject(beanObject));
    }

    // 添加用户自定义的对象
    public void addBean(String name, IocObject object) {
        Validate.notNull(name);
        Validate.notNull(object);

        log.debug("addBean: {}", name);

        if (pool.put(name, object) != null) {
            log.warn("Duplicated Bean: {}", name);
        }
    }

    // 注册 @IocBean 标注的对象
    public void addBean(Class<?> beanClass) {
        addBean(beanClass, null, true);
    }

    // 注册 @IocBean 标注的对象
    public void addBean(Class<?> beanClass, Configuration properties, boolean singleton) {
        Validate.notNull(beanClass);
        addBean(beanClass.getName(), beanClass, properties, singleton);
    }

    // 注册 @IocBean 标注的对象
    public void addBean(String name, Class<?> beanClass, Configuration properties, boolean singleton) {
        Validate.notNull(name);
        Validate.notNull(beanClass);
        Validate.isFalse(beanClass.isInterface(), "Must not be interface: %s", beanClass.getName());
        Validate.isFalse(Modifier.isAbstract(beanClass.getModifiers()), "Must not be abstract class: %s", beanClass.getName());

        log.debug("addBean: {}", name, beanClass.getName());

        IocObject iocObject = doGetIocObject(beanClass, properties, singleton);
        if (pool.put(name, iocObject) != null) {
            log.warn("Duplicated Bean: {}", name);
        }
    }

    private IocObject doGetIocObject(Class<?> beanClass, Configuration properties, boolean singleton) {
        if (IocFactory.class.isAssignableFrom(beanClass)) {
            if (singleton) {
                return new FactorySingletonObject(this, beanClass, properties);
            } else {
                return new FactoryInstanceObject(this, beanClass, properties);
            }
        } else {
            if (singleton) {
                return new ClassSingletonObject(this, beanClass, properties);
            } else {
                return new ClassInstanceObject(this, beanClass, properties);
            }
        }
    }

    // 获取一个 Bean
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> beanClass) {
        Validate.notNull(beanClass);
        return (T) getBean(beanClass.getName());
    }

    // 获取一个 Bean
    @Override
    public Object getBean(String name) {
        IocObject iocObject = pool.get(name);
        if (iocObject == null) {
            return null;
        }
        return iocObject.getObject();
    }

    @Override
    public <T> T getConfig(String name, Class<T> type) {
        return config.getValue(name, type);
    }

    @Override
    public <T> T getConfig(String name, Class<T> type, T defaultValue) {
        return config.getValue(name, type, defaultValue);
    }

    @Override
    public <T> T getConfig(String name, Class<T> type, String defaultValue) {
        return config.getValue(name, type, defaultValue);
    }

    @Override
    public Object getConfigAsArray(String name, Class<?> elementType) {
        return config.getValueArray(name, elementType);
    }

    @Override
    public <T> List<T> getConfigAsList(String name, Class<T> elementType) {
        return config.getValueList(name, elementType);
    }

    @Override
    public <T> T injectClass(Class<T> beanClass) {
        return injectClass(beanClass, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T injectClass(Class<T> beanClass, Configuration properties) {
        Validate.notNull(beanClass);

        IocObject iocObject = doGetIocObject(beanClass, properties, true);
        return (T) iocObject.getObject();
    }

    @Override
    public <T> T injectInstance(T beanObject) {
        return injectInstance(beanObject, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T injectInstance(T beanObject, Configuration properties) {
        Validate.notNull(beanObject);

        IocObject iocObject = new UninitialedObject(this, beanObject, properties);
        return (T) iocObject.getObject();
    }
}
