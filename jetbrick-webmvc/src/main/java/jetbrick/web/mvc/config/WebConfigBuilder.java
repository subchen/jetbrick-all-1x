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
package jetbrick.web.mvc.config;

import java.lang.annotation.Annotation;
import java.util.*;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import jetbrick.commons.beans.TypeResolverUtils;
import jetbrick.commons.config.Configuration;
import jetbrick.commons.config.ConfigurationLoader;
import jetbrick.commons.io.finder.ClassFinder;
import jetbrick.commons.lang.StringUtils;
import jetbrick.ioc.Ioc;
import jetbrick.ioc.MutableIoc;
import jetbrick.ioc.annotations.IocBean;
import jetbrick.ioc.annotations.Managed;
import jetbrick.ioc.loaders.IocAnnotationLoader;
import jetbrick.ioc.loaders.IocPropertiesLoader;
import jetbrick.web.mvc.*;
import jetbrick.web.mvc.action.ArgumentGetterResolver;
import jetbrick.web.mvc.action.Controller;
import jetbrick.web.mvc.action.annotations.ArgumentGetter;
import jetbrick.web.mvc.results.ResultHandler;
import jetbrick.web.mvc.results.views.ViewHandler;
import jetbrick.web.servlet.ServletUtils;

public class WebConfigBuilder {

    public static WebConfig build(FilterConfig fc) {
        ServletContext sc = fc.getServletContext();

        // get config file
        String configLocation = fc.getInitParameter("configLocation");
        if (StringUtils.isEmpty(configLocation)) {
            configLocation = "/WEB-INF/jetbrick-webmvc.properties";
        }

        // load config file
        ConfigurationLoader configurationLoader = new ConfigurationLoader();
        configurationLoader.loadSystemEnvs();
        configurationLoader.loadSystemProperties();
        configurationLoader.load("web.root", ServletUtils.getWebroot(sc).getAbsolutePath());
        configurationLoader.load("web.upload.dir", "${java.io.tmpdir}");
        configurationLoader.loadSerlvetResource(sc, configLocation);
        configurationLoader.placeholder();
        Configuration config = configurationLoader.getConfiguration();

        // scan components
        List<String> packageNames = config.asStringList("web.scan.packages");
        Set<Class<?>> componentKlasses = discoveryComponents(packageNames);

        // create ioc container
        MutableIoc ioc = new MutableIoc();
        ioc.addBean(Ioc.class.getName(), ioc);
        ioc.addBean(ServletContext.class.getName(), sc);
        ioc.addBean(WebConfig.class, null, true);
        ioc.addBean(ResultHandlerResolver.class, null, true);
        ioc.addBean(ViewHandlerResolver.class, null, true);
        ioc.addBean(ArgumentGetterResolver.class, null, true);
        ioc.load(new IocAnnotationLoader(componentKlasses));
        ioc.load(new IocPropertiesLoader(config));

        // put into servletContext
        sc.setAttribute(Ioc.class.getName(), ioc);

        // create the webconfig instance and inject all fields
        WebConfig webConfig = ioc.getBean(WebConfig.class);

        // register others
        registerManagedComponments(ioc, componentKlasses);
        registerControllers(webConfig.getRouter(), componentKlasses);

        return webConfig;
    }

    private static Set<Class<?>> discoveryComponents(List<String> packageNames) {
        List<Class<? extends Annotation>> annotationList = new ArrayList<Class<? extends Annotation>>();
        annotationList.add(IocBean.class);
        annotationList.add(Controller.class);
        annotationList.add(Managed.class);
        return ClassFinder.getClasses(packageNames, true, annotationList, true);
    }

    private static void registerManagedComponments(Ioc ioc, Collection<Class<?>> klasses) {
        ResultHandlerResolver resultHandlerResolver = ioc.getBean(ResultHandlerResolver.class);
        ViewHandlerResolver viewHandlerResolver = ioc.getBean(ViewHandlerResolver.class);
        ArgumentGetterResolver argumentGetterResolver = ioc.getBean(ArgumentGetterResolver.class);

        for (Class<?> klass : klasses) {
            for (Annotation annotation : klass.getAnnotations()) {
                if (annotation instanceof Managed) {
                    if (ResultHandler.class.isAssignableFrom(klass)) {
                        Class<?>[] managedClasses = ((Managed) annotation).value();
                        if (managedClasses.length == 0) {
                            Class<?> type = TypeResolverUtils.getGenericSupertype(klass);
                            resultHandlerResolver.register(type, klass);
                        } else {
                            for (Class<?> type : managedClasses) {
                                resultHandlerResolver.register(type, klass);
                            }
                        }
                    } else if (ViewHandler.class.isAssignableFrom(klass)) {
                        viewHandlerResolver.register(klass);
                    } else if (ArgumentGetter.class.isAssignableFrom(klass)) {
                        Class<?>[] managedClasses = ((Managed) annotation).value();
                        if (managedClasses.length == 0) {
                            Class<?> type = TypeResolverUtils.getGenericSupertype(klass);
                            argumentGetterResolver.register(type, klass);
                        } else {
                            for (Class<?> type : managedClasses) {
                                argumentGetterResolver.register(type, klass);
                            }
                        }
                    } else {
                        throw new IllegalStateException("@Managed annotation is illegal in class: " + klass.getName());
                    }
                }
            }
        }
    }

    private static void registerControllers(Router router, Set<Class<?>> klasses) {
        for (Class<?> klass : klasses) {
            Controller controller = klass.getAnnotation(Controller.class);
            if (controller != null) {
                router.registerController(klass);
            }
        }
    }
}
