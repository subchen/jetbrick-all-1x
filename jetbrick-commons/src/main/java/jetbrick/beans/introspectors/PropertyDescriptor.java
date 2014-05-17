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
package jetbrick.beans.introspectors;

import java.lang.reflect.InvocationTargetException;

public class PropertyDescriptor {
    private final ClassDescriptor classDescriptor;
    private final FieldDescriptor fieldDescriptor;
    private MethodDescriptor readMethodDescriptor;
    private MethodDescriptor writeMethodDescriptor;
    private String name;

    public PropertyDescriptor(ClassDescriptor classDescriptor, FieldDescriptor fieldDescriptor) {
        this.classDescriptor = classDescriptor;
        this.fieldDescriptor = fieldDescriptor;
    }

    protected void setReadMethodDescriptor(MethodDescriptor descriptor) {
        this.readMethodDescriptor = descriptor;
    }

    protected void setWriteMethodDescriptor(MethodDescriptor descriptor) {
        this.writeMethodDescriptor = descriptor;
    }

    public ClassDescriptor getClassDescriptor() {
        return classDescriptor;
    }

    public FieldDescriptor getFieldDescriptor() {
        return fieldDescriptor;
    }

    public MethodDescriptor getReadMethodDescriptor() {
        return readMethodDescriptor;
    }

    public MethodDescriptor getWriteMethodDescriptor() {
        return writeMethodDescriptor;
    }

    public String getName() {
        if (name == null) {
            if (readMethodDescriptor != null) {
                name = readMethodDescriptor.getPropertyName();
            } else if (writeMethodDescriptor != null) {
                name = writeMethodDescriptor.getPropertyName();
            } else if (fieldDescriptor != null) {
                name = fieldDescriptor.getName();
            } else {
                throw new IllegalStateException("Invalid PropertyDescriptor.");
            }
        }
        return name;
    }

    public Class<?> getRawType() {
        if (readMethodDescriptor != null) {
            return readMethodDescriptor.getRawReturnType();
        }
        if (writeMethodDescriptor != null) {
            return writeMethodDescriptor.getRawParameterTypes()[0];
        }
        if (fieldDescriptor != null) {
            return fieldDescriptor.getRawType();
        }
        throw new IllegalStateException("Invalid PropertyDescriptor: " + toString());
    }

    public Class<?> getRawComponentType(int index) {
        if (readMethodDescriptor != null) {
            return readMethodDescriptor.getRawReturnComponentType(index);
        }
        if (writeMethodDescriptor != null) {
            return writeMethodDescriptor.getRawParameterComponentType(0, index);
        }
        if (fieldDescriptor != null) {
            return fieldDescriptor.getRawComponentType(index);
        }
        throw new IllegalStateException("Invalid PropertyDescriptor: " + toString());
    }

    public boolean readable() {
        if (readMethodDescriptor != null) {
            return true;
        }
        if (fieldDescriptor != null && !fieldDescriptor.isFinal()) {
            return true;
        }
        return false;
    }

    public boolean writable() {
        if (writeMethodDescriptor != null) {
            return true;
        }
        if (fieldDescriptor != null && !fieldDescriptor.isFinal()) {
            return true;
        }
        return false;
    }

    public Object invokeGetter(Object bean) {
        try {
            if (readMethodDescriptor != null) {
                return readMethodDescriptor.getMethod().invoke(bean, (Object[]) null);
            }
            if (fieldDescriptor != null) {
                return fieldDescriptor.invokeGetter(bean);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        throw new IllegalStateException("Property is not readable: " + toString());
    }

    public void invokeSetter(Object bean, Object value) {
        try {
            if (writeMethodDescriptor != null) {
                writeMethodDescriptor.getMethod().invoke(bean, value);
                return;
            }
            if (fieldDescriptor != null) {
                fieldDescriptor.invokeSetter(bean, value);
                return;
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        throw new IllegalStateException("Property is not writable: " + toString());
    }

    @Override
    public String toString() {
        return classDescriptor.getType().getName() + '#' + getName();
    }
}
