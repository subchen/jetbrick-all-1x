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

import java.util.*;
import org.junit.Assert;
import org.junit.Test;

public class KlassInfoConstructorTest {

    @Test
    public void create() {
        KlassInfo k1 = KlassInfo.create(HashMap.class);
        KlassInfo k2 = KlassInfo.create(HashMap.class);
        KlassInfo k3 = KlassInfo.create(AbstractMap.class);

        Assert.assertTrue(k1 == k2);
        Assert.assertEquals(HashMap.class, k1.getType());
        Assert.assertEquals("java.util.HashMap", k1.getName());
        Assert.assertEquals("HashMap", k1.getSimpleName());

        Assert.assertEquals(k3, k1.getSuperKlass());
        Assert.assertEquals(3, k1.getInterfaces().size());
    }

    @Test
    public void getDeclaredConstructors() {
        KlassInfo klass = KlassInfo.create(HashMap.class);
        List<ConstructorInfo> constructors = klass.getDeclaredConstructors();
        Assert.assertEquals(4, constructors.size());
    }

    @Test
    public void getDeclaredConstructor() {
        KlassInfo klass = KlassInfo.create(HashMap.class);
        Assert.assertNotNull(klass.getDefaultConstructor());
        Assert.assertNotNull(klass.getDeclaredConstructor());
        Assert.assertNotNull(klass.getDeclaredConstructor(int.class));
        Assert.assertNotNull(klass.getDeclaredConstructor(Map.class));

        Assert.assertNull(klass.getDeclaredConstructor(short.class));
        Assert.assertNotNull(klass.searchDeclaredConstructor(short.class));
    }

    @Test
    public void getDeclaredMethods() {
        KlassInfo klass = KlassInfo.create(Object.class);
        Assert.assertEquals(12, klass.getDeclaredMethods().size());
    }

    @Test
    public void getDeclaredMethodsWithFilter() {
        KlassInfo k1 = KlassInfo.create(Object.class);
        List<MethodInfo> methods = k1.getDeclaredMethods(Filters.PUBLIC_METHOD);
        Assert.assertEquals(9, methods.size());

        KlassInfo k2 = KlassInfo.create(UUID.class);
        methods = k2.getDeclaredMethods(Filters.STATIC_METHOD);
        Assert.assertEquals(4, methods.size());
    }

    @Test
    public void getDeclaredMethod() {
        KlassInfo klass = KlassInfo.create(Map.class);
        Assert.assertNotNull(klass.getDeclaredMethod("get", Object.class));
        Assert.assertNotNull(klass.getDeclaredMethod("put", Object.class, Object.class));

        Assert.assertNull(klass.getDeclaredMethod("get"));
        Assert.assertNull(klass.getDeclaredMethod("get", String.class));
    }

    @Test
    public void getMethods() {
        KlassInfo k1 = KlassInfo.create(UUID.class);
        Assert.assertEquals(27, k1.getMethods().size());

        KlassInfo k2 = KlassInfo.create(List.class);
        Assert.assertEquals(47, k2.getMethods().size());
    }

    @Test
    public void getMethodsWithFilter() {
        KlassInfo klass = KlassInfo.create(UUID.class);
        List<MethodInfo> methods = klass.getMethods(Filters.PUBLIC_METHOD);
        Assert.assertEquals(24, methods.size());

        methods = klass.getMethods(Filters.INSTANCE_METHOD);
        Assert.assertEquals(23, methods.size());
    }

    @Test
    public void getMethod() {
        KlassInfo klass = KlassInfo.create(HashMap.class);
        Assert.assertNotNull(klass.getMethod("get", Object.class));
        Assert.assertNotNull(klass.getMethod("toString"));

        Assert.assertNull(klass.getMethod("get", String.class));
        Assert.assertNotNull(klass.searchMethod("get", String.class));
    }
}
