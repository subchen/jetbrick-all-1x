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
package jetbrick.web.mvc;

import java.util.IdentityHashMap;
import java.util.Map;
import jetbrick.beans.ClassUtils;
import jetbrick.ioc.Ioc;
import jetbrick.ioc.annotations.*;
import jetbrick.lang.Validate;
import jetbrick.web.mvc.results.*;
import com.alibaba.fastjson.JSONAware;
import com.google.gson.JsonElement;

/**
 * 全局 ResultHandler 管理器
 */
public class ResultHandlerResolver {
    private final Map<Class<?>, ResultHandler<?>> mapping = new IdentityHashMap<Class<?>, ResultHandler<?>>();

    @Inject
    private Ioc ioc;

    @IocInit
    public void initialize() {
        register(Void.TYPE, VoidResultHandler.class);
        register(Object.class, ObjectResultHandler.class);
        register(String.class, StringResultHandler.class);
        register(HttpStatus.class, HttpStatusResultHandler.class);
        register(RawData.class, RawDataResultHandler.class);
        register(RawDownload.class, RawDownloadResultHandler.class);

        if (ClassUtils.available("com.alibaba.fastjson.JSONAware")) {
            register(JSONAware.class, FastjsonResultHandler.class);
        }
        if (ClassUtils.available("com.google.gson.JsonElement")) {
            register(JsonElement.class, GsonResultHandler.class);
        }
    }

    public void register(Class<?> resultClass, Class<?> resultHandlerClass) {
        Validate.isAssignableFrom(ResultHandler.class, resultHandlerClass);

        ResultHandler<?> resultHandler = (ResultHandler<?>) ioc.injectClass(resultHandlerClass);
        mapping.put(resultClass, resultHandler);
    }

    @SuppressWarnings("unchecked")
    public ResultHandler<Object> lookup(Class<?> resultClass) {
        ResultHandler<Object> result = (ResultHandler<Object>) mapping.get(resultClass);
        if (result == null) {
            // Special code for Object.class as result
            for (Map.Entry<Class<?>, ResultHandler<?>> entry : mapping.entrySet()) {
                Class<?> targetClass = entry.getKey();
                if (targetClass != Object.class && targetClass.isAssignableFrom(resultClass)) {
                    return (ResultHandler<Object>) entry.getValue();
                }
            }
            throw new IllegalStateException("Unsupported result class: " + resultClass.getName());
        }
        return result;
    }

    public boolean supported(Class<?> resultClass) {
        if (mapping.containsKey(resultClass)) {
            return true;
        }
        ManagedWith with = resultClass.getAnnotation(ManagedWith.class);
        if (with != null && ResultHandler.class.isAssignableFrom(with.value())) {
            register(resultClass, with.value());
            return true;
        }
        return false;
    }
}
