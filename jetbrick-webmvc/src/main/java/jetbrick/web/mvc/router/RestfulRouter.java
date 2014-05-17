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
package jetbrick.web.mvc.router;

import javax.servlet.http.HttpServletRequest;
import jetbrick.beans.introspectors.ClassDescriptor;
import jetbrick.beans.introspectors.MethodDescriptor;
import jetbrick.lang.*;
import jetbrick.web.mvc.*;
import jetbrick.web.mvc.action.*;
import jetbrick.web.mvc.action.annotations.ValueConstants;
import jetbrick.web.mvc.config.WebConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h2>URL 映射规则：</h2>
 * <ul>
 *   <li>/users + (null)  == /users/(method)</li>
 *   <li>/users + (empty) == /users/(method)</li>
 *   <li>/users + /       == /users/</li>
 *   <li>/users + /add    == /users/add</li>
 * </ul>
 */
public final class RestfulRouter implements Router {
    private final Logger log = LoggerFactory.getLogger(RestfulRouter.class);
    private final RestfulMatcher[] matchers = new RestfulMatcher[HttpMethod.METHOD_LENGTH];

    @Override
    public void registerController(Class<?> klass) {
        Controller controller = klass.getAnnotation(Controller.class);
        Validate.notNull(controller);

        String ctrlPath = ValueConstants.defaultValue(controller.value(), "");
        ControllerInfo ctrlInfo = new ControllerInfo(klass, controller);

        ResultHandlerResolver resultHandlerResolver = WebConfig.getInstance().getResultHandlerResolver();
        ClassDescriptor meta = ClassDescriptor.lookup(klass);
        for (MethodDescriptor md : meta.getMethodDescriptors()) {
            if (!md.isPublic() || md.isStatic()) {
                continue;
            }
            Action action = md.getAnnotation(Action.class);
            if (action == null) {
                continue;
            }
            String actionPath = ValueConstants.defaultValue(action.value(), md.getName());
            String url = StringUtils.removeEnd(ctrlPath, "/") + StringUtils.prefix(actionPath, "/");

            // validate the action result type
            if (!resultHandlerResolver.supported(md.getRawReturnType())) {
                throw new IllegalStateException("Unsupported result class for method: " + md);
            }

            HttpMethod[] methods = action.method();
            Validate.isTrue(methods.length > 0);

            if (log.isDebugEnabled()) {
                log.debug("found action: {} {}", ArrayUtils.toString(methods), url);
            }
            ActionInfo actionInfo = new ActionInfo(ctrlInfo, md, url);
            for (HttpMethod method : methods) {
                RestfulMatcher matcher = matchers[method.getIndex()];
                if (matcher == null) {
                    matcher = new RestfulMatcher();
                    matchers[method.getIndex()] = matcher;
                }
                matcher.register(actionInfo, url);
            }
        }
    }

    @Override
    public RouteInfo lookup(HttpServletRequest request, String path, HttpMethod method) {
        RestfulMatcher matcher = matchers[method.getIndex()];
        if (matcher != null) {
            return matcher.lookup(path);
        }
        return RouteInfo.NOT_FOUND;
    }
}
