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
package jetbrick.reflect.asm;

import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import jetbrick.lang.UnsafeUtils;

final class ASMClassLoader extends ClassLoader {
    private static final String ASMAccessor_KLASS_NAME = ASMAccessor.class.getName();
    private static final List<ASMClassLoader> asmClassLoaders = new ArrayList<ASMClassLoader>();

    // Fast-path for classes loaded in the same ClassLoader as this class.
    private static final ClassLoader parentASMClassLoader = ASMClassLoader.class.getClassLoader();
    private static final ASMClassLoader defaultASMClassLoader = new ASMClassLoader(parentASMClassLoader);

    public static ASMClassLoader get(Class<?> type) {
        ClassLoader parent = type.getClassLoader();
        // 1. fast-path:
        if (parentASMClassLoader == parent) {
            return defaultASMClassLoader;
        }
        // 2. normal search:
        synchronized (asmClassLoaders) {
            for (int i = 0, n = asmClassLoaders.size(); i < n; i++) {
                ASMClassLoader loader = asmClassLoaders.get(i);
                if (loader.getParent() == parent) {
                    return loader;
                }
            }
            ASMClassLoader loader = new ASMClassLoader(parent);
            asmClassLoaders.add(loader);
            return loader;
        }
    }

    private ASMClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    protected synchronized java.lang.Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // These classes come from the class loader that loaded AccessClassLoader.
        if (name.equals(ASMAccessor_KLASS_NAME)) return ASMAccessor.class;

        // All other classes come from the class loader that loaded the type we are accessing.
        return super.loadClass(name, resolve);
    }

    protected Class<?> defineClass(String qualifiedClassName, byte[] bytes, ProtectionDomain protectionDomain) throws ClassFormatError {
        // method 1:
        try {
            return UnsafeUtils.defineClass(qualifiedClassName, bytes, 0, bytes.length, this, protectionDomain);
        } catch (Throwable e) {
        }

        // method 2:
        /*
        try {
            // Attempt to load the access class in the same loader, which makes protected and default access members accessible.
            // this method shoud be cached.
            Method method = ClassLoader.class.getDeclaredMethod("defineClass", new Class[] { String.class, byte[].class, int.class, int.class, ProtectionDomain.class });
            method.setAccessible(true);
            return (Class<?>) method.invoke(getParent(), new Object[] { name, bytes, 0, bytes.length, protectionDomain });
        } catch (Throwable e) {
        }
         */

        // method 3:
        return defineClass(qualifiedClassName, bytes, 0, bytes.length, protectionDomain);
    }
}
