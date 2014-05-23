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
package jetbrick.dao.orm;

import javax.servlet.http.HttpServletRequest;
import jetbrick.web.servlet.RequestUtils;

public final class PageInfoImpl implements PageInfo {
    private int pageNo;
    private int pageSize;
    private int totalCount;
    private String pageUrl;

    public static PageInfo create(HttpServletRequest request) {
        PageInfoImpl info = new PageInfoImpl();
        info.pageNo = RequestUtils.getParameterAsInteger(request, "page", 1);
        info.pageSize = RequestUtils.getParameterAsInteger(request, "pageSize", 20);
        info.totalCount = RequestUtils.getParameterAsInteger(request, "totalCount", 0);
        info.pageUrl = RequestUtils.getUrlWithParameters(request, null);
        return info;
    }

    @Override
    public int getPageNo() {
        return pageNo;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public int getTotalCount() {
        return totalCount;
    }

    @Override
    public String getPageUrl() {
        return pageUrl;
    }
}
