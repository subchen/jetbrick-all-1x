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

import java.io.File;
import jetbrick.io.IoUtils;
import jetbrick.reflect.KlassInfo;
import org.slf4j.LoggerFactory;

public final class ASMFactory {
    private static int ASM_THRESHOLD_VALUE = 5;
    private static boolean ASM_DEBUG_ENABLED = false;

    public static int getThreshold() {
        return ASM_THRESHOLD_VALUE;
    }

    public static void setThreshold(int value) {
        ASM_THRESHOLD_VALUE = value;
    }

    public static void setDebugEnabled(boolean enabled) {
        ASM_DEBUG_ENABLED = enabled;
    }

    public static ASMAccessor generateAccessor(Class<?> delegateKlass) {
        return generateAccessor(KlassInfo.create(delegateKlass));
    }

    public static ASMAccessor generateAccessor(KlassInfo delegateKlass) {
        Class<?> delegateType = delegateKlass.getType();
        String generatedKlassName = ASMFactory.class.getPackage().getName() + ".delegate." + delegateType.getName().replace('.', '_');

        Class<?> generatedKlass;
        ASMClassLoader loader = ASMClassLoader.get(delegateType);
        synchronized (loader) {
            try {
                generatedKlass = loader.loadClass(generatedKlassName);
            } catch (ClassNotFoundException e) {
                byte[] byteCode = ASMBuilder.create(generatedKlassName, delegateKlass);
                if (ASM_DEBUG_ENABLED) {
                    File dir = new File(System.getProperty("java.io.tmpdir"));
                    File file = new File(dir, generatedKlassName.replace('.', '/') + ".class");
                    file.getParentFile().mkdirs();

                    LoggerFactory.getLogger(ASMFactory.class).info("ASMFactory generated {}", file);
                    IoUtils.write(byteCode, file);
                }
                generatedKlass = loader.defineClass(generatedKlassName, byteCode, delegateType.getProtectionDomain());
            }
        }

        try {
            return (ASMAccessor) generatedKlass.newInstance();
        } catch (Throwable e) {
            throw new RuntimeException("Error constructing access class: " + generatedKlassName, e);
        }
    }
}
