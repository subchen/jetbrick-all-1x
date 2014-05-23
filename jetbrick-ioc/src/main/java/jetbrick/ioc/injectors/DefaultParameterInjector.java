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
package jetbrick.ioc.injectors;

import java.lang.annotation.Annotation;
import jetbrick.ioc.Ioc;
import jetbrick.ioc.annotations.IocConstants;
import jetbrick.reflect.KlassInfo;
import jetbrick.reflect.ParameterInfo;

// 注入没有任何标注的参数
public class DefaultParameterInjector implements ParameterInjector {
    private Ioc ioc;
    private String name;

    @Override
    public void initialize(Ioc ioc, KlassInfo declaringKlass, ParameterInfo parameter, Annotation annotation) {
        this.ioc = ioc;
        this.name = parameter.getRawType(declaringKlass).getName();
    }

    @Override
    public Object getObject() throws Exception {
        Object value = ioc.getBean(name);
        if (value == null && IocConstants.REQUIRED) {
            throw new IllegalStateException("Can't inject parameter.");
        }
        return value;
    }
}
