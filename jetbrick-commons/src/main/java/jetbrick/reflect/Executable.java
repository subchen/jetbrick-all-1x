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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public interface Executable {

    public KlassInfo getDeclaringKlass();

    public String getName();

    public int getOffset();

    public ParameterInfo[] getParameters();

    public int getParameterCount();

    public Class<?>[] getParameterTypes();

    public Type[] getGenericParameterTypes();

    public boolean isVarArgs();

    public Annotation[] getAnnotations();

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass);

    public <T extends Annotation> boolean isAnnotationPresent(Class<T> annotationClass);

    public Annotation[][] getParameterAnnotations();

    public int getModifiers();

    public boolean isPrivate();

    public boolean isProtected();

    public boolean isPublic();

    public String getDescriptor();

    static class ExecutableUtils {
        public static ParameterInfo[] getParameterInfo(Executable object) {
            Class<?>[] parameterTypes = object.getParameterTypes();
            if (parameterTypes.length == 0) {
                return ParameterInfo.EMPTY_ARRAY;
            }
            Type[] genericParameterTypes = object.getGenericParameterTypes();
            Annotation[][] parameterAnnotations = object.getParameterAnnotations();

            ParameterInfo[] parameters = new ParameterInfo[genericParameterTypes.length];
            for (int i = 0; i < parameters.length; i++) {
                parameters[i] = new ParameterInfo(object, parameterTypes[i], genericParameterTypes[i], parameterAnnotations[i], i);
            }
            return parameters;
        }

        public static String getDescriptor(Executable object) {
            StringBuffer sb = new StringBuffer();
            sb.append(object.getName()).append('(');
            Class<?>[] parameterTypes = object.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(parameterTypes[i].getName());
            }
            sb.append(')');
            return sb.toString();
        }
    }
}
