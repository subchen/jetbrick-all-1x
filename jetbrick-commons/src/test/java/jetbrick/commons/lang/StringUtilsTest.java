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
package jetbrick.commons.lang;

import java.util.ArrayList;
import java.util.List;
import jetbrick.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest {

    public static String[] splitCSV(String str) {
        if (str == null) return null;
        String[] parts = StringUtils.split(str, ',');

        List<String> results = new ArrayList<String>();
        for (int i = 0; i < parts.length; i++) {
            String s = parts[i].trim();
            if (s.length() == 0) {
                results.add(s);
            } else {
                char c = s.charAt(0);
                if (c == '"' || c == '\'' || c == '`') {
                    StringBuilder sb = new StringBuilder();
                    sb.append(s);
                    while (++i < parts.length) {
                        if (sb.length() > 1 && s.length() > 0 && s.charAt(s.length() - 1) == c) {
                            break;
                        }
                        s = parts[i];
                        sb.append(',').append(s);
                    }
                    s = sb.toString().trim();
                    if (s.charAt(s.length() - 1) == c) {
                        s = s.substring(1, s.length() - 1);
                    }
                    results.add(s);
                } else {
                    results.add(s);
                }
            }
        }
        return results.toArray(new String[results.size()]);
    }

    @Test
    public void testSplitQuote() {
        Assert.assertArrayEquals(new String[] { "aa" }, splitCSV("aa"));
        Assert.assertArrayEquals(new String[] { "aa", "bb" }, splitCSV("aa, bb"));
        Assert.assertArrayEquals(new String[] { "aa", "bb", "cc, dd" }, splitCSV("aa, bb, 'cc, dd'"));
        Assert.assertArrayEquals(new String[] { "aa", "bb", ",cc,, dd," }, splitCSV("aa, bb, ',cc,, dd,'"));
    }

}
