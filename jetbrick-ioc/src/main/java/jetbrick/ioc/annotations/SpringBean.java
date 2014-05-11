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
package jetbrick.ioc.annotations;

import java.lang.annotation.*;
import jetbrick.ioc.injectors.SpringBeanFieldInjector;
import jetbrick.ioc.injectors.SpringBeanParameterInjector;

/**
 * 将 Spring Ioc 容器中的 Bean 注入.
 *
 * @author Guoqiang Chen
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@InjectFieldWith(SpringBeanFieldInjector.class)
@InjectParameterWith(SpringBeanParameterInjector.class)
public @interface SpringBean {

    String value();

    boolean required() default ValueConstants.REQUIRED;

}
