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

import java.util.*;
import jetbrick.lang.JSONUtils;

public final class PagelistImpl<T> implements Pagelist<T> {
    private final int pageNo;
    private final int pageSize;
    private int totalCount;
    private List<T> items;
    private String pageUrl;

    public PagelistImpl(PageInfo page) {
        this.pageNo = page.getPageNo();
        this.pageSize = page.getPageSize();
        this.totalCount = page.getTotalCount();
        this.items = Collections.emptyList();
        this.pageUrl = page.getPageUrl();
    }

    @Override
    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
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
    public int getFirstResult() {
        return (pageNo - 1) * pageSize;
    }

    @Override
    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    @Override
    public int getPageCount() {
        if (totalCount > 0) {
            return (totalCount - 1) / pageSize + 1;
        } else {
            return 1;
        }
    }

    @Override
    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    @Override
    public boolean isEmpty() {
        return totalCount > 0;
    }

    @Override
    public boolean isFirstPage() {
        return pageNo == 1;
    }

    @Override
    public boolean isLastPage() {
        return pageNo == getPageCount();
    }

    @Override
    public String toJSONString() {
        Map<String, Object> json = new HashMap<String, Object>();
        json.put("pageNo", pageNo);
        json.put("pageSize", pageSize);
        json.put("pageCount", getPageCount());
        json.put("items", items);
        json.put("totalCount", totalCount);
        json.put("pageUrl", pageUrl);
        return JSONUtils.toJSONString(json);
    }

    @Override
    public String toString() {
        return toJSONString();
    }
}
