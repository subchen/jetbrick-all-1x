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

import jetbrick.beans.ClassLoaderUtils;
import jetbrick.commons.config.Configuration;
import jetbrick.ioc.Ioc;
import jetbrick.typecast.TypeCastUtils;

public final class IocConfiguration extends Configuration {
    private static final long serialVersionUID = 1L;
    private final Ioc ioc;

    public IocConfiguration(Ioc ioc) {
        this.ioc = ioc;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> T cast(String value, Class<T> type) {
        if (value.startsWith("$")) {
            T object = (T) ioc.getBean(value.substring(1));
            if (object == null) {
                throw new IllegalStateException("cannot find the defination: " + value);
            }
            return object;
        } else {
            try {
                return TypeCastUtils.convert(value, type);
            } catch (IllegalStateException e) {
                try {
                    Class<?> klass = ClassLoaderUtils.loadClassEx(value);
                    return (T) ioc.injectClass(klass);
                } catch (ClassNotFoundException ex) {
                    throw e; // IllegalStateException
                }
            }
        }
    }
}
