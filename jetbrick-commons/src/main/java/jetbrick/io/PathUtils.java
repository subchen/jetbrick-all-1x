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
package jetbrick.io;

import java.io.File;

public class PathUtils {

    /**
     * Returns normalized <code>path</code> (or simply the <code>path</code> if
     * it is already in normalized form). Normalized path does not contain any
     * empty or "." segments or ".." segments preceded by other segment than
     * "..".
     *
     * @param path path to normalize
     * @return normalize path
     */
    public static String normalize(final String path) {
        if (path == null) {
            return null;
        }
        if (path.indexOf('.') == -1 && path.indexOf('/') == -1) {
            return path;
        }

        boolean wasNormalized = true;

        // 1. count number of nonempty segments in path
        // Note that this step is not really necessary because we could simply
        // estimate the number of segments as path.length() and do the empty
        // segment check in step two ;-).
        int numSegments = 0;
        int lastChar = path.length() - 1;
        for (int src = lastChar; src >= 0;) {
            int slash = path.lastIndexOf('/', src);
            if (slash != -1) {
                // empty segment? (two adjacent slashes?)
                if (slash == src) {
                    if (src != lastChar) { // ignore the first slash occurence
                        // (when numSegments == 0)
                        wasNormalized = false;
                    }
                } else {
                    numSegments++;
                }
            } else {
                numSegments++;
            }
            src = slash - 1;
        }

        // 2. split path to segments skipping empty segments
        int[] segments = new int[numSegments];
        char[] chars = new char[path.length()];
        path.getChars(0, chars.length, chars, 0);
        numSegments = 0;
        for (int src = 0; src < chars.length;) {
            // skip empty segments
            while (src < chars.length && chars[src] == '/') {
                src++;
            }
            if (src < chars.length) {
                // note the segment start
                segments[numSegments++] = src;
                // seek to the end of the segment
                while (src < chars.length && chars[src] != '/') {
                    src++;
                }
            }
        }
        // assert (numSegments == segments.length);

        // 3. scan segments and remove all "." segments and "foo",".." segment pairs
        final int DELETED = -1;
        for (int segment = 0; segment < numSegments; segment++) {
            int src = segments[segment];
            if (chars[src++] == '.') {
                if (src == chars.length || chars[src] == '/') { // "." or"./"
                    // delete the "." segment
                    segments[segment] = DELETED;
                    wasNormalized = false;
                } else { // ".something"
                    if (chars[src++] == '.' && (src == chars.length || chars[src] == '/')) { // ".." or "../"
                        // we have the ".." segment scan backwards for segment to delete together with ".."
                        for (int toDelete = segment - 1; toDelete >= 0; toDelete--) {
                            if (segments[toDelete] != DELETED) {
                                if (chars[segments[toDelete]] != '.') {
                                    // delete the two segments
                                    segments[toDelete] = DELETED;
                                    segments[segment] = DELETED;
                                    wasNormalized = false;
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

        // 4. join the result, if necessary
        if (wasNormalized) { // already normalized? nothing to do...
            return path;
        } else {
            // join the resulting normalized path, retain the leading and ending slash
            int dst = (chars[0] == '/') ? 1 : 0;
            for (int segment = 0; segment < numSegments; segment++) {
                int segmentStart = segments[segment];
                if (segmentStart != DELETED) {
                    // if we remembered segment lengths in step 2, we could use
                    // System.arraycopy method now but we had to allocate one
                    // more array
                    for (int src = segmentStart; src < chars.length; src++) {
                        char ch = chars[src];
                        chars[dst++] = ch;
                        if (ch == '/') {
                            break;
                        }
                    }
                }
            }
            return new String(chars, 0, dst);
        }
    }

    /**
     * 组合路径.
     */
    public static String concat(final String parent, final String child) {
        if (parent == null) {
            return normalize(child);
        }
        if (child == null) {
            return normalize(parent);
        }
        return normalize(parent + '/' + child);
    }

    /**
     * 计算相对路径.
     */
    public static String getRelativePath(final String path, final String relativePath) {
        if (relativePath.startsWith("/")) {
            return normalize(relativePath);
        }
        int separatorIndex = path.lastIndexOf('/');
        if (separatorIndex != -1) {
            String newPath = path.substring(0, separatorIndex + 1);
            return normalize(newPath + relativePath);
        } else {
            return normalize(relativePath);
        }
    }

    /**
     * 转为 Unix 样式的路径.
     */
    public static String separatorsToUnix(String path) {
        if (path == null || path.indexOf('\\') == -1) {
            return path;
        }
        return path.replace('\\', '/');
    }

    /**
     * 转为 Windows 样式的路径.
     */
    public static String separatorsToWindows(String path) {
        if (path == null || path.indexOf('/') == -1) {
            return path;
        }
        return path.replace('/', '\\');
    }

    /**
     * 转为系统默认样式的路径.
     */
    public static String separatorsToSystem(String path) {
        if (path == null) {
            return null;
        }
        if (File.separatorChar == '\\') {
            return separatorsToWindows(path);
        }
        return separatorsToUnix(path);
    }
}
