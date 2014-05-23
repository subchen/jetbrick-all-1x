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
import javax.servlet.ServletContext;
import jetbrick.ioc.Ioc;
import jetbrick.ioc.annotations.SpringBean;
import jetbrick.lang.Validate;
import jetbrick.lang.annotations.ValueConstants;
import jetbrick.reflect.FieldInfo;
import jetbrick.reflect.KlassInfo;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

//注入 @SpringBean 标注的字段
public class SpringBeanFieldInjector implements FieldInjector {
    private ApplicationContext appctx;
    private String name;
    private FieldInfo field;
    private boolean required;

    @Override
    public void initialize(Ioc ioc, KlassInfo declaringKlass, FieldInfo field, Annotation annotation) {
        Validate.isInstanceOf(SpringBean.class, annotation);

        SpringBean inject = (SpringBean) annotation;
        this.appctx = getApplicationContext(ioc);
        this.field = field;
        this.name = ValueConstants.defaultValue(inject.value(), field.getRawType(declaringKlass).getName());
        this.required = inject.required();
    }

    private ApplicationContext getApplicationContext(Ioc ioc) {
        ApplicationContext ctx = ioc.getBean(ApplicationContext.class);
        if (ctx != null) {
            return ctx;
        }

        ServletContext sc = ioc.getBean(ServletContext.class);
        if (sc != null) {
            ctx = WebApplicationContextUtils.getWebApplicationContext(sc);
        }

        if (ctx == null) {
            throw new IllegalStateException("No Spring Found!");
        }

        return ctx;
    }

    @Override
    public void set(Object object) throws Exception {
        Object value = appctx.getBean(name);
        if (value == null && required) {
            throw new IllegalStateException("Can't inject bean: " + name + " for field: " + object.getClass().getName() + '#' + field.getName());
        }
        field.set(object, value);
    }
}
