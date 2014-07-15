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

import java.io.File;

public class SystemUtils {
    private static final String USER_HOME_KEY = "user.home";
    private static final String USER_DIR_KEY = "user.dir";
    private static final String JAVA_IO_TMPDIR_KEY = "java.io.tmpdir";
    private static final String JAVA_HOME_KEY = "java.home";

    public static final String JAVA_CLASS_PATH = System.getProperty("java.class.path");
    public static final String JAVA_CLASS_VERSION = System.getProperty("java.class.version");
    public static final String JAVA_COMPILER = System.getProperty("java.compiler");
    public static final String JAVA_ENDORSED_DIRS = System.getProperty("java.endorsed.dirs");
    public static final String JAVA_EXT_DIRS = System.getProperty("java.ext.dirs");
    public static final String JAVA_HOME = System.getProperty("java.home");
    public static final String JAVA_IO_TMPDIR = System.getProperty("java.io.tmpdir");
    public static final String JAVA_LIBRARY_PATH = System.getProperty("java.library.path");
    public static final String JAVA_RUNTIME_NAME = System.getProperty("java.runtime.name");
    public static final String JAVA_RUNTIME_VERSION = System.getProperty("java.runtime.version");
    public static final String JAVA_SPECIFICATION_NAME = System.getProperty("java.specification.name");
    public static final String JAVA_SPECIFICATION_VENDOR = System.getProperty("java.specification.vendor");
    public static final String JAVA_SPECIFICATION_VERSION = System.getProperty("java.specification.version");
    public static final String JAVA_UTIL_PREFS_PREFERENCES_FACTORY = System.getProperty("java.util.prefs.PreferencesFactory");
    public static final String JAVA_VENDOR = System.getProperty("java.vendor");
    public static final String JAVA_VENDOR_URL = System.getProperty("java.vendor.url");
    public static final String JAVA_VERSION = System.getProperty("java.version");
    public static final String JAVA_VM_INFO = System.getProperty("java.vm.info");
    public static final String JAVA_VM_NAME = System.getProperty("java.vm.name");
    public static final String JAVA_VM_SPECIFICATION_NAME = System.getProperty("java.vm.specification.name");
    public static final String JAVA_VM_SPECIFICATION_VENDOR = System.getProperty("java.vm.specification.vendor");
    public static final String JAVA_VM_SPECIFICATION_VERSION = System.getProperty("java.vm.specification.version");
    public static final String JAVA_VM_VENDOR = System.getProperty("java.vm.vendor");
    public static final String JAVA_VM_VERSION = System.getProperty("java.vm.version");
    public static final String OS_ARCH = System.getProperty("os.arch");
    public static final String OS_NAME = System.getProperty("os.name");
    public static final String OS_VERSION = System.getProperty("os.version");
    public static final String USER_COUNTRY = System.getProperty("user.country") == null ? System.getProperty("user.region") : System.getProperty("user.country");
    public static final String USER_DIR = System.getProperty("user.dir");
    public static final String USER_HOME = System.getProperty("user.home");
    public static final String USER_LANGUAGE = System.getProperty("user.language");
    public static final String USER_NAME = System.getProperty("user.name");
    public static final String USER_TIMEZONE = System.getProperty("user.timezone");
    public static final String FILE_ENCODING = System.getProperty("file.encoding");
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String PATH_SEPARATOR = System.getProperty("path.separator");

    public static final boolean IS_OS_WINDOWS = (File.separatorChar == '\\');
    public static final boolean IS_OS_UNIX = (File.separatorChar == '/');

    public static File getJavaHome() {
        return new File(System.getProperty(JAVA_HOME_KEY));
    }

    public static File getJavaIoTmpDir() {
        return new File(System.getProperty(JAVA_IO_TMPDIR_KEY));
    }

    public static File getUserDir() {
        return new File(System.getProperty(USER_DIR_KEY));
    }

    public static File getUserHome() {
        return new File(System.getProperty(USER_HOME_KEY));
    }
}
