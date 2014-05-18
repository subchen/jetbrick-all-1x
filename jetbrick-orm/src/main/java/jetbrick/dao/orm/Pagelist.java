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

import java.util.List;

public interface Pagelist<T> extends PageInfo {

    /**
     * 分页开始记录（从 0 开始, 等价于 (pageNo-1)*pageSize）
     */
    public int getFirstResult();

    /**
     * 总页数
     */
    public int getPageCount();

    /**
     * 当前页记录
     */
    public List<T> getItems();

    /**
     * 分页 URL
     */
    @Override
    public String getPageUrl();

    /**
     * 是否为空
     */
    public boolean isEmpty();

    /**
     * 是否第一页
     */
    public boolean isFirstPage();

    /**
     * 是否最后一页
     */
    public boolean isLastPage();

    /**
     * 转成 JSON 字符串
     */
    public String toJSONString();
}
