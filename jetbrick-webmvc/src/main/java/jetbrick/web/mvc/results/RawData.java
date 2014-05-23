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

import java.io.*;
import jetbrick.io.streams.UnsafeByteArrayInputStream;
import jetbrick.ioc.annotations.ManagedWith;
import jetbrick.web.mvc.RequestContext;

@ManagedWith(RawDataResultHandler.class)
public final class RawData {
    private final InputStream is;
    private final String contentType;
    private final int contentLength;

    public static RawData html(String data) {
        return raw(data, "text/html", null);
    }

    public static RawData html(String data, String encoding) {
        return raw(data, "text/html", encoding);
    }

    public static RawData text(String data) {
        return raw(data, "text/plain", null);
    }

    public static RawData text(String data, String encoding) {
        return raw(data, "text/plain", encoding);
    }

    public static RawData xml(String data) {
        return raw(data, "text/xml", null);
    }

    public static RawData xml(String data, String encoding) {
        return raw(data, "text/xml", encoding);
    }

    public static RawData js(String data) {
        return raw(data, "text/javascript", null);
    }

    public static RawData js(String data, String encoding) {
        return raw(data, "text/javascript", encoding);
    }

    public static RawData css(String data) {
        return raw(data, "text/css", null);
    }

    public static RawData css(String data, String encoding) {
        return raw(data, "text/css", encoding);
    }

    public static RawData raw(String data, String mimetype, String encoding) {
        if (encoding == null) {
            encoding = RequestContext.getCurrent().getResponse().getCharacterEncoding();
        }
        String contentType = mimetype + "; charset=" + encoding;

        try {
            return new RawData(data.getBytes(encoding), contentType);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public RawData(InputStream is, String contentType) {
        this.is = is;
        this.contentType = contentType;
        this.contentLength = 0;
    }

    public RawData(File file, String contentType) {
        try {
            this.is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        this.contentType = contentType;
        this.contentLength = (int) file.length();
    }

    public RawData(byte[] data, String contentType) {
        this.is = new UnsafeByteArrayInputStream(data);
        this.contentType = contentType;
        this.contentLength = data.length;
    }

    public InputStream getInputStream() {
        return is;
    }

    public String getContentType() {
        return contentType;
    }

    public int getContentLength() {
        return contentLength;
    }
}
