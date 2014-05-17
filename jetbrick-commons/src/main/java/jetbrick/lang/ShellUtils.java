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
package jetbrick.lang;

import java.io.*;
import java.util.Map;
import jetbrick.io.IoUtils;
import jetbrick.io.output.UnsafeByteArrayOutputStream;

/**
 * Java 调用外部命令，并获取输出 (解决了 IO 阻塞问题).
 */
public class ShellUtils {

    /**
     * shell("ls -l")
     */
    public static Result shell(String command) {
        return shell(command, null, null);
    }

    public static Result shell(String command, File directory, Map<String, String> envp) {
        if (SystemUtils.IS_OS_WINDOWS) {
            return execute(directory, envp, "cmd.exe", "/c", command);
        } else {
            return execute(directory, envp, "/bin/sh", "-c", command);
        }
    }

    /**
     * shell("ls", "-l")
     */
    public static Result execute(String... command) {
        return execute(null, null, command);
    }

    public static Result execute(File directory, Map<String, String> envp, String... command) {
        ProcessBuilder pb = new ProcessBuilder(command);
        if (directory != null) {
            pb.directory(directory);
        }
        if (envp != null) {
            pb.environment().putAll(envp);
        }

        Result result = new Result();
        Process p = null;
        try {
            p = pb.start();
            new InputStreamReadThread("shell-exec-stdout", p.getInputStream(), result.stdout).start();
            new InputStreamReadThread("shell-exec-stderr", p.getErrorStream(), result.stderr).start();
            p.waitFor();
            result.exitValue = p.exitValue();
        } catch (Throwable e) {
            result.error = e;
        } finally {
            if (p != null) {
                p.destroy();
            }
        }
        return result;
    }

    static final class Result {
        int exitValue = -99;
        UnsafeByteArrayOutputStream stdout = new UnsafeByteArrayOutputStream();
        UnsafeByteArrayOutputStream stderr = new UnsafeByteArrayOutputStream();
        Throwable error;

        public boolean good() {
            return error == null && exitValue == 0;
        }

        public int exitValue() {
            return exitValue;
        }

        public String stdout() {
            return stdout.toString();
        }

        public String stdout(String charset) {
            try {
                return stdout.toString(charset);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        public String stderr() {
            return stderr.toString();
        }

        public String stderr(String charset) {
            try {
                return stderr.toString(charset);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        public Throwable error() {
            return error;
        }
    }

    static final class InputStreamReadThread extends Thread {
        final InputStream is;
        final OutputStream os;

        InputStreamReadThread(String name, InputStream is, OutputStream os) {
            super(name);
            this.setDaemon(true);
            this.is = is;
            this.os = os;
        }

        @Override
        public void run() {
            try {
                int n = is.read();
                while (n > -1) {
                    os.write(n);
                    n = is.read();
                }
            } catch (IOException e) {
                // hit stream eof, do nothing
            } finally {
                IoUtils.closeQuietly(is);
            }
        }
    }
}
