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
package jetbrick.web.mvc.results;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jetbrick.ioc.annotations.Managed;
import jetbrick.web.mvc.RequestContext;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONObject;

@Managed(JSONAware.class)
public class FastJSONResultHandler implements ResultHandler<JSONAware> {

    @Override
    public void handle(RequestContext ctx, JSONAware result) throws IOException {
        HttpServletRequest request = ctx.getRequest();
        HttpServletResponse response = ctx.getResponse();

        if (result == null) {
            JSONObject json = new JSONObject();
            Enumeration<String> e = request.getAttributeNames();
            while (e.hasMoreElements()) {
                String name = e.nextElement();
                json.put(name, request.getAttribute(name));
            }
            for (Map.Entry<String, Object> entry : ctx.getModel().entrySet()) {
                json.put(entry.getKey(), entry.getValue());
            }
            result = json;
        }

        String characterEncoding = request.getCharacterEncoding();
        response.setCharacterEncoding(characterEncoding);
        if (isOldIeBrowser(request)) {
            response.setContentType("text/html;charset=" + characterEncoding);
        } else {
            response.setContentType("application/json;charset=" + characterEncoding);
        }
        PrintWriter out = response.getWriter();
        out.write(result.toJSONString());
        out.flush();
    }

    // IE 10 以下的版本不支持 application/json
    private boolean isOldIeBrowser(HttpServletRequest request) {
        try {
            String agent = request.getHeader("user-agent");
            int ipos = agent.indexOf("MSIE");
            if (ipos > 0) {
                ipos = ipos + 4;
                int jpos = agent.indexOf(';', ipos);
                String version = agent.substring(ipos, jpos);
                return Float.parseFloat(version) < 10;
            }
        } catch (Throwable e) {
            return false;
        }
        return false;
    }
}
