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

import java.lang.reflect.Field;
import java.security.ProtectionDomain;
import sun.misc.Unsafe;

/**
 * javac uses a special symbol table that does not include all Sun-proprietary classes,
 * and suppliying -XDignore.symbol.file makes the problem go away.
  */
@SuppressWarnings("restriction")
public final class UnsafeUtils {
    private final static Unsafe unsafe;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("unsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T allocateInstance(Class<T> klass) throws InstantiationException {
        return (T) unsafe.allocateInstance(klass);
    }

    public static void throwException(Throwable e) {
        unsafe.throwException(e);
    }

    public static Class<?> defineClass(String name, byte[] bytes, int offset, int size, ClassLoader loader, ProtectionDomain protectionDomain) {
        return unsafe.defineClass(name, bytes, offset, size, loader, protectionDomain);
    }
}
