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
package jetbrick.commons.debug;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import jetbrick.io.IoUtils;
import jetbrick.lang.*;

public class VMs {

    public static String getProcessId() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        return StringUtils.substringBefore(name, "@");
    }

    public static String getThreadDump() {
        if (JdkVersion.IS_AT_LEAST_JAVA_6) {
            return "Java AppVersionUtils must be equal or larger than 1.6";
        }
        String jstack = "../bin/jstack";
        if (SystemUtils.IS_OS_WINDOWS) {
            jstack = jstack + ".exe";
        }
        try {
            File jstackFile = new File(System.getProperty("java.home"), jstack);
            String command = jstackFile.getCanonicalPath() + " " + VMs.getProcessId();
            Process process = Runtime.getRuntime().exec(command);
            return IoUtils.toString(process.getInputStream(), "ISO-8859-1");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean detectDeadlock() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        long[] threadIds = threadBean.findDeadlockedThreads();
        return (threadIds != null && threadIds.length > 0);
    }
}
