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
import java.io.FileOutputStream;
import jetbrick.reflect.KlassInfo;
import org.slf4j.LoggerFactory;

public final class ASMFactory {
    public static final boolean IS_ASM_ENABLED = System.getProperty("jetbrick.asm.enabled") != null;
    public static final boolean IS_ASM_DEBUG = System.getProperty("jetbrick.asm.debug") != null;

    public static ASMConstructorAccessor generateConstructorAccessor(KlassInfo delegateKlass) {
        return (ASMConstructorAccessor) generateAccessor(delegateKlass, CONSTRUCTOR_GENERATOR);
    }

    public static ASMConstructorAccessor generateConstructorAccessor(Class<?> delegateKlass) {
        return generateConstructorAccessor(KlassInfo.create(delegateKlass));
    }

    public static ASMMethodAccessor generateMethodAccessor(KlassInfo delegateKlass) {
        return (ASMMethodAccessor) generateAccessor(delegateKlass, METHOD_GENERATOR);
    }

    public static ASMMethodAccessor generateMethodAccessor(Class<?> delegateKlass) {
        return generateMethodAccessor(KlassInfo.create(delegateKlass));
    }

    public static ASMFieldAccessor generateFieldAccessor(KlassInfo delegateKlass) {
        return (ASMFieldAccessor) generateAccessor(delegateKlass, FIELD_GENERATOR);
    }

    public static ASMFieldAccessor generateFieldAccessor(Class<?> delegateKlass) {
        return generateFieldAccessor(KlassInfo.create(delegateKlass));
    }

    private static Object generateAccessor(KlassInfo delegateKlass, ByteCodeGenerator byteCodeGenerator) {
        Class<?> delegateType = delegateKlass.getType();
        String generatedKlassName = ASMFactory.class.getPackage().getName() + "." + delegateType.getName().replace('.', '_') + "_" + byteCodeGenerator.getName();

        Class<?> generatedKlass;
        ASMClassLoader loader = ASMClassLoader.get(delegateType);
        synchronized (loader) {
            try {
                generatedKlass = loader.loadClass(generatedKlassName);
            } catch (ClassNotFoundException e) {
                byte[] code = byteCodeGenerator.generate(delegateKlass, generatedKlassName);
                if (IS_ASM_DEBUG) {
                    try {
                        File dir = new File(System.getProperty("java.io.tmpdir"));
                        File file = new File(dir, generatedKlassName.replace('.', '/') + ".class");
                        LoggerFactory.getLogger(ASMFactory.class).info("ASMFactory generated {}", generatedKlassName);

                        file.getParentFile().mkdirs();
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(code);
                        fos.close();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
                generatedKlass = loader.defineClass(generatedKlassName, code, delegateType.getProtectionDomain());
            }
        }

        try {
            return generatedKlass.newInstance();
        } catch (Throwable e) {
            throw new RuntimeException("Error constructing access class: " + generatedKlassName, e);
        }
    }

    static interface ByteCodeGenerator {
        public String getName();

        public byte[] generate(KlassInfo delegateKlass, String generatedNameInternal);
    }

    static final ByteCodeGenerator CONSTRUCTOR_GENERATOR = new ByteCodeGenerator() {
        @Override
        public String getName() {
            return "ConstructorAccessor";
        }

        @Override
        public byte[] generate(KlassInfo delegateKlass, String generatedKlassName) {
            ASMBuilder builder = new ASMBuilder(generatedKlassName, delegateKlass.getName(), ASMConstructorAccessor.class);
            builder.insertArgumentsLengthField(delegateKlass.getDeclaredConstructors());
            builder.insertCheckArgumentsMethod();
            builder.insertConstructor();
            builder.insertNewInstance();
            builder.insertNewInstance(delegateKlass.getDeclaredConstructors());
            return builder.asByteCode();
        }
    };

    static final ByteCodeGenerator METHOD_GENERATOR = new ByteCodeGenerator() {
        @Override
        public String getName() {
            return "MethodAccessor";
        }

        @Override
        public byte[] generate(KlassInfo delegateKlass, String generatedKlassName) {
            ASMBuilder builder = new ASMBuilder(generatedKlassName, delegateKlass.getName(), ASMMethodAccessor.class);
            builder.insertArgumentsLengthField(delegateKlass.getDeclaredMethods());
            builder.insertCheckArgumentsMethod();
            builder.insertConstructor();
            builder.insertInvoke(delegateKlass.getDeclaredMethods());
            return builder.asByteCode();
        }
    };

    static final ByteCodeGenerator FIELD_GENERATOR = new ByteCodeGenerator() {
        @Override
        public String getName() {
            return "FieldAccessor";
        }

        @Override
        public byte[] generate(KlassInfo delegateKlass, String generatedKlassName) {
            ASMBuilder builder = new ASMBuilder(generatedKlassName, delegateKlass.getName(), ASMFieldAccessor.class);
            builder.insertConstructor();
            builder.insertGetObject(delegateKlass.getDeclaredFields());
            builder.insertSetObject(delegateKlass.getDeclaredFields());
            return builder.asByteCode();
        }
    };

}
