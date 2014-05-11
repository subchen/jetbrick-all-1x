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
package jetbrick.web.mvc.results.views;

import jetbrick.commons.lang.StringUtils;
import jetbrick.web.mvc.RequestContext;

public abstract class AbstractTemplateViewHandler implements ViewHandler {
    protected String prefix;
    protected String suffix;

    @Override
    public String getViewPrefix() {
        return prefix;
    }

    @Override
    public String getViewSuffix() {
        return suffix;
    }

    public void setViewPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setViewSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public boolean render(RequestContext ctx, String viewPathName) throws Throwable {
        String view;
        if (viewPathName.endsWith("/")) {
            view = viewPathName.concat("index");
        } else {
            view = viewPathName;
        }
        view = StringUtils.suffix(view, suffix);

        return doRender(ctx, view);
    }

    protected abstract boolean doRender(RequestContext ctx, String viewPathName) throws Throwable;

}
