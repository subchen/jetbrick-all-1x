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

import java.util.List;
import jetbrick.commons.config.Configuration;

public interface Ioc {

    public Object getBean(String name);

    public <T> T getBean(Class<T> type);

    public <T> T getConfig(String name, Class<T> type);

    public <T> T getConfig(String name, Class<T> type, T defaultValue);

    public <T> T getConfig(String name, Class<T> type, String defaultValue);

    public Object getConfigAsArray(String name, Class<?> elementType);

    public <T> List<T> getConfigAsList(String name, Class<T> elementType);

    public <T> T injectClass(Class<T> beanClass);

    public <T> T injectClass(Class<T> beanClass, Configuration properties);

    public <T> T injectInstance(T beanObject);

    public <T> T injectInstance(T beanObject, Configuration properties);
}
