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
package jetbrick.ioc.loaders;

import java.util.Iterator;
import java.util.Map.Entry;
import jetbrick.commons.beans.ClassLoaderUtils;
import jetbrick.commons.config.Configuration;
import jetbrick.ioc.MutableIoc;
import jetbrick.ioc.objects.ClassSingletonObject;
import jetbrick.ioc.objects.IocObject;
import jetbrick.lang.StringUtils;

public class IocPropertiesLoader implements IocLoader {
    private Configuration config;

    public IocPropertiesLoader(Configuration config) {
        this.config = config;
    }

    @Override
    public void load(MutableIoc ioc) {

        // find all definations
        Iterator<Entry<String, String>> it = config.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, String> entry = it.next();
            String name = entry.getKey();
            String value = entry.getValue();

            if (name != null) {
                if (name.startsWith("$") && name.indexOf('.') == -1) {
                    if (StringUtils.isBlank(value)) {
                        throw new IllegalStateException("blank value for `" + name + "`");
                    }
                    Class<?> beanClass;
                    try {
                        beanClass = ClassLoaderUtils.loadClassEx(value);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    Configuration properties = config.subset(name);
                    IocObject ref = new ClassSingletonObject(ioc, beanClass, properties);
                    ioc.addBean(name.substring(1), ref);
                }
            }
        }

        // process others
        for (Entry<String, String> entry : config.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (name != null && value != null) {
                if (!name.startsWith("$")) {
                    ioc.addConfig(name, value);
                }
            }
        }

        // free
        config = null;
    }
}
