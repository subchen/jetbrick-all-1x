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
package jetbrick.web.servlet.utils;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.ServletContext;
import javax.servlet.http.*;
import jetbrick.commons.codec.Base64;
import jetbrick.commons.lang.*;

public abstract class ServletUtils {

    public static File getWebroot(ServletContext sc) {
        String dir = sc.getRealPath("/");
        if (dir == null) {
            try {
                URL url = sc.getResource("/");
                if (url != null && "file".equals(url.getProtocol())) {
                    dir = URLDecoder.decode(url.getFile(), "utf-8");
                }
                throw new IllegalStateException("Can't get webroot dir, url = " + url);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return new File(dir);
    }

    /**
     * 获取相对 ContextPath 的 requestURI
     */
    public static String getRequestPathInfo(HttpServletRequest request) {
        String path = request.getPathInfo();
        if (path == null) {
            path = request.getServletPath();
        } else {
            path = request.getServletPath() + path;
        }
        if (path == null || path.length() == 0) {
            path = "/";
        }
        return path;
    }

    public static String getRequestContextFullPath(HttpServletRequest request) {
        String schema = request.getScheme();
        int port = request.getServerPort();

        StringBuilder sb = new StringBuilder();
        sb.append(schema);
        sb.append("://");
        sb.append(request.getServerName());
        if (!(port == 80 && "http".equals(schema)) && !(port == 443 && "https".equals(schema))) {
            sb.append(':').append(request.getServerPort());
        }
        sb.append(request.getContextPath());
        return sb.toString();
    }

    public static String getParametersAsJSON(HttpServletRequest request) {
        Map<String, Object> json = new HashMap<String, Object>();
        Enumeration<String> enu = request.getParameterNames();
        while (enu.hasMoreElements()) {
            String name = enu.nextElement();
            String[] value = request.getParameterValues(name);
            if (value == null || value.length == 0) {
                continue;
            }
            if (value.length > 1) {
                json.put(name, value);
            } else {
                json.put(name, value[0]);
            }
        }
        return JSONUtils.toJSONString(json);
    }

    public static String getUrlParameters(HttpServletRequest request, String excludeNames) {
        StringBuffer sb = new StringBuffer();
        String[] excludeNamesArray = StringUtils.split(excludeNames, ',');

        Enumeration<String> enu = request.getParameterNames();
        while (enu.hasMoreElements()) {
            String name = enu.nextElement();
            if (ArrayUtils.contains(excludeNamesArray, name)) continue;

            String[] value = request.getParameterValues(name);
            if (value == null) continue;
            for (int i = 0; i < value.length; i++) {
                try {
                    if (sb.length() > 0) sb.append("&");
                    sb.append(name).append('=').append(URLEncoder.encode(value[i], request.getCharacterEncoding()));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (sb.length() > 0) sb.insert(0, '?');
        sb.insert(0, request.getContextPath() + request.getServletPath());

        return sb.toString();
    }

    public static String getClientIPAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("x-real-ip");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public static void setBufferOff(HttpServletResponse response) {
        // Http 1.0 header
        response.setHeader("Buffer", "false");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 1L);
        // Http 1.1 header
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0");
    }

    public static void setFileDownloadHeader(HttpServletResponse response, String fileName, String contentType) {
        if (contentType == null) contentType = "application/x-download";
        response.setContentType(contentType);

        // 中文文件名支持
        try {
            String encodedfileName = new String(fileName.getBytes(), "ISO8859-1");
            response.setHeader("Content-Disposition", "attachment; filename=" + encodedfileName);
        } catch (UnsupportedEncodingException e) {
        }
    }

    // 是否是Ajax请求数据
    public static boolean isAjaxRequest(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With")) || request.getHeader("accept").contains("application/json");
    }

    // 是否是Pjax请求数据: https://github.com/defunkt/jquery-pjax
    public static boolean isPjaxRequest(HttpServletRequest request) {
        return StringUtils.isNotEmpty(request.getHeader("X-PJAX"));
    }

    // 是否是Flash请求数据
    public static boolean isFlashRequest(HttpServletRequest request) {
        return "Shockwave Flash".equals(request.getHeader("User-Agent")) || StringUtils.isNotEmpty(request.getHeader("x-flash-version"));
    }

    // 是否是文件上传
    public static boolean isMultipartRequest(HttpServletRequest request) {
        String type = request.getHeader("Content-Type");
        return (type != null) && (type.startsWith("multipart/form-data"));
    }

    public static boolean isGzipSupported(HttpServletRequest request) {
        String browserEncodings = request.getHeader("Accept-Encoding");
        return (browserEncodings != null) && (browserEncodings.contains("gzip"));
    }

    // 判断是否为搜索引擎
    public static boolean isRobot(HttpServletRequest request) {
        String ua = request.getHeader("user-agent");
        if (StringUtils.isBlank(ua)) return false;
        //@formatter:off
	    return (ua != null
               && (ua.indexOf("Baiduspider") != -1
                || ua.indexOf("Googlebot") != -1
                || ua.indexOf("sogou") != -1
                || ua.indexOf("sina") != -1
                || ua.indexOf("iaskspider") != -1
                || ua.indexOf("ia_archiver") != -1
                || ua.indexOf("Sosospider") != -1
                || ua.indexOf("YoudaoBot") != -1
                || ua.indexOf("yahoo") != -1
                || ua.indexOf("yodao") != -1
                || ua.indexOf("MSNBot") != -1
                || ua.indexOf("spider") != -1
                || ua.indexOf("Twiceler") != -1
                || ua.indexOf("Sosoimagespider") != -1
                || ua.indexOf("naver.com/robots") != -1
                || ua.indexOf("Nutch") != -1
                || ua.indexOf("spider") != -1));
	    }
	    //@formatter:on

    /**
     * 客户端对Http Basic验证的 Header进行编码.
     */
    public static String encodeHttpBasic(String userName, String password) {
        String encode = userName + ":" + password;
        return "Basic " + Base64.encodeToString(encode.getBytes());
    }

    public static String getAuthUsername(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null) {
            return null;
        }
        String encoded = header.substring(header.indexOf(' ') + 1);
        String decoded = Base64.decodeToString(encoded);
        return decoded.substring(0, decoded.indexOf(':'));
    }

    public static String getAuthPassword(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null) {
            return null;
        }
        String encoded = header.substring(header.indexOf(' ') + 1);
        String decoded = Base64.decodeToString(encoded);
        return decoded.substring(decoded.indexOf(':') + 1);
    }

    public static String dump(HttpServletRequest request) {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);

        final String FORMAT = "%20s: %s%n";
        final char PADDING_CHAR = '=';
        final int PADDING_SIZE = 60;

        out.println(StringUtils.center(" Request Basic ", PADDING_SIZE, PADDING_CHAR));
        out.printf(FORMAT, "Request Date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z").format(new Date()));
        out.printf(FORMAT, "Request URL", request.getRequestURL());
        out.printf(FORMAT, "QueryString", request.getQueryString());
        out.printf(FORMAT, "Method", request.getMethod());
        out.println();

        out.printf(FORMAT, "CharacterEncoding", request.getCharacterEncoding());
        out.printf(FORMAT, "ContentType", request.getContentType());
        out.printf(FORMAT, "ContentLength", request.getContentLength());
        out.printf(FORMAT, "Locale", request.getLocale());
        out.printf(FORMAT, "RemoteAddr", request.getRemoteAddr());
        out.println();

        out.println(StringUtils.center(" Request Headers ", PADDING_SIZE, PADDING_CHAR));
        Enumeration<String> header = request.getHeaderNames();
        while (header.hasMoreElements()) {
            String name = header.nextElement();
            String value = request.getHeader(name);
            out.printf(FORMAT, name, value);
        }
        out.println();

        out.println(StringUtils.center(" Request Parameters ", PADDING_SIZE, PADDING_CHAR));
        Enumeration<String> param = request.getParameterNames();
        while (param.hasMoreElements()) {
            String name = param.nextElement();
            String value[] = request.getParameterValues(name);
            out.printf(FORMAT, name, StringUtils.join(value, ", "));
        }
        out.println();

        out.println(StringUtils.center(" Request Cookies ", PADDING_SIZE, PADDING_CHAR));
        for (Cookie cookie : request.getCookies()) {
            out.printf(FORMAT, cookie.getName(), cookie.getValue());
        }
        out.println();

        out.println(StringUtils.repeat(PADDING_CHAR, PADDING_SIZE));
        out.flush();

        return sw.toString();
    }
}
