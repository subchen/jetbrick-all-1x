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
package jetbrick.web.mvc.action;

import jetbrick.reflect.MethodInfo;
import jetbrick.web.mvc.RequestContext;
import jetbrick.web.mvc.ResultInfo;
import jetbrick.web.mvc.router.UrlTemplate;

public final class ActionInfo {
    private final ControllerInfo controller;
    private final MethodInfo action;
    private final UrlTemplate urlTemplate;
    private ActionMethodInjector methodInjector;
    private boolean initialized;

    public ActionInfo(ControllerInfo controller, MethodInfo action, String url) {
        this.controller = controller;
        this.action = action;
        this.urlTemplate = new UrlTemplate(url);
        this.initialized = false;
    }

    private void initialize() throws Exception {
        if (initialized == false) {
            synchronized (this) {
                if (initialized == false) {
                    methodInjector = ActionMethodInjector.create(action, controller.getType());
                    initialized = true;
                }
            }
        }
    }

    // 和实际的 URL 进行匹配，并返回成功匹配的参数(pathVariables)
    public boolean match(String[] urlSegments, PathVariables pathVariables) {
        return urlTemplate.match(urlSegments, pathVariables);
    }

    public ResultInfo execute(RequestContext ctx) throws Exception {
        initialize();

        Object object = controller.getObject();
        Object result = methodInjector.invoke(object, ctx);

        return new ResultInfo(action.getRawReturnType(controller.getType()), result);
    }
}
