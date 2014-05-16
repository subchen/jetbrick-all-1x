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
package jetbrick.reflect;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import jetbrick.asm.*;
import jetbrick.commons.beans.ClassLoaderUtils;
import jetbrick.commons.beans.TypeResolverUtils;

public final class ParameterInfo {
    public static final ParameterInfo[] EMPTY_ARRAY = new ParameterInfo[0];

    private final Executable declaringExecutable;
    private final Class<?> type;
    private final Type genericType;
    private final Annotation[] annotations;
    private final int offset;
    private String name;

    protected ParameterInfo(Executable declaringExecutable, Class<?> type, Type genericType, Annotation[] annotations, int offset) {
        this.declaringExecutable = declaringExecutable;
        this.type = type;
        this.genericType = genericType;
        this.annotations = annotations;
        this.offset = offset;
    }

    public String getName() {
        if (name == null) {
            KlassInfo declaringklass = declaringExecutable.getDeclaringKlass();
            synchronized (declaringklass) {
                receiveParameterNames(declaringklass);
                if (name == null) {
                    name = "arg" + String.valueOf(offset);
                }
            }
        }
        return name;
    }

    public Executable getDeclaringExecutable() {
        return declaringExecutable;
    }

    public int getOffset() {
        return offset;
    }

    public Class<?> getType() {
        return type;
    }

    public Type getGenericType() {
        return genericType;
    }

    public Class<?> getRawType(Class<?> declaringKlass) {
        return TypeResolverUtils.getRawType(genericType, declaringKlass);
    }

    public Class<?> getRawComponentType(Class<?> declaringKlass, int componentIndex) {
        return TypeResolverUtils.getComponentType(genericType, declaringKlass, componentIndex);
    }

    public Annotation[] getAnnotations() {
        return annotations;
    }

    @SuppressWarnings("unchecked")
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        for (Annotation annotation : annotations) {
            if (annotationClass == annotation.annotationType()) {
                return (T) annotation;
            }
        }
        return null;
    }

    public <T extends Annotation> boolean isAnnotationPresent(Class<T> annotationClass) {
        return (getAnnotation(annotationClass) != null);
    }

    @Override
    public String toString() {
        return type.getName() + " " + (name == null ? "arg" + String.valueOf(offset) : name);
    }

    // 使用 ASM 获取参数名称
    private static void receiveParameterNames(final KlassInfo declaringklass) {
        if (declaringklass.getType().getClassLoader() == null) {
            // cannot find parameter name for class in class
            return;
        }

        ClassReader cr = null;
        try {
            InputStream stream = ClassLoaderUtils.getClassAsStream(declaringklass.getType());
            cr = new ClassReader(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        cr.accept(new ClassVisitor(Opcodes.ASM5) {
            @Override
            public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
                final MethodInfo method = searchMethod(declaringklass, name, desc);
                if (method == null) {
                    return super.visitMethod(access, name, desc, signature, exceptions);
                }

                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                return new MethodVisitor(Opcodes.ASM5, mv) {
                    ParameterInfo[] parameters = method.getParameters();
                    int parameterIndex = 0;

                    @Override
                    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                        int offset = method.isStatic() ? index : index - 1;
                        if (offset >= 0 && offset < parameters.length) {
                            parameters[offset].name = name;
                        }
                        super.visitLocalVariable(name, desc, signature, start, end, index);
                    }

                    @Override
                    public void visitParameter(String name, int access) {
                        parameters[parameterIndex++].name = name;
                        super.visitParameter(name, access);
                    }
                };
            }

            private MethodInfo searchMethod(KlassInfo declaringklass, String name, String desc) {
                if ("<cinit>".equals(name)) return null;
                if ("<init>".equals(name)) return null;

                jetbrick.asm.Type[] argumentTypes = jetbrick.asm.Type.getArgumentTypes(desc);
                for (MethodInfo method : declaringklass.getDeclaredMethods()) {
                    if (method.getName().equals(name) && argumentTypes.length == method.getParameterCount()) {
                        Class<?>[] types = method.getParameterTypes();
                        boolean matched = true;
                        for (int i = 0; i < argumentTypes.length; i++) {
                            if (!jetbrick.asm.Type.getType(types[i]).equals(argumentTypes[i])) {
                                matched = false;
                                break;
                            }
                        }
                        if (matched) {
                            return method;
                        }
                    }
                }
                return null;
            }
        }, 0);
    }
}
