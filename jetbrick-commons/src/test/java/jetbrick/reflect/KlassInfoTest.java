package jetbrick.reflect;

import java.util.*;
import org.junit.*;

public class KlassInfoTest {
    KlassInfo klass;

    @Before
    public void setup() {
        klass = KlassInfo.create(HashMap.class);
    }

    @Test
    public void getDeclaredConstructors() {
        List<ConstructorInfo> constructors = klass.getDeclaredConstructors();
        Assert.assertEquals(4, constructors.size());
    }

    @Test
    public void getDeclaredConstructor() {
        Assert.assertNotNull(klass.getDeclaredConstructor());
        Assert.assertNotNull(klass.getDeclaredConstructor(int.class));
        Assert.assertNotNull(klass.getDeclaredConstructor(Map.class));
        Assert.assertNull(klass.getDeclaredConstructor(long.class));
    }

    @Test
    public void getDeclaredMethods() {
        List<MethodInfo> methods = klass.getDeclaredMethods();
        Assert.assertEquals(47, methods.size());
    }

    @Test
    public void getDeclaredMethodsWithFilter() {
        List<MethodInfo> methods = klass.getDeclaredMethods(Filters.PUBLIC_METHOD);
        Assert.assertEquals(24, methods.size());

        methods = klass.getDeclaredMethods(Filters.STATIC_METHOD);
        Assert.assertEquals(4, methods.size());
    }

    @Test
    public void getDeclaredMethod() {
        Assert.assertNull(klass.getDeclaredMethod("get"));
        Assert.assertNotNull(klass.getDeclaredMethod("get", Object.class));
        Assert.assertNotNull(klass.getDeclaredMethod("put", Object.class, Object.class));
    }

    @Test
    public void getMethods() {
        List<MethodInfo> methods = klass.getMethods();
        Assert.assertEquals(77, methods.size());
    }

    @Test
    public void getMethodsWithFilter() {
        List<MethodInfo> methods = klass.getMethods(Filters.PUBLIC_METHOD);
        Assert.assertEquals(48, methods.size());

        methods = klass.getMethods(Filters.STATIC_METHOD);
        Assert.assertEquals(7, methods.size());
    }

    @Test
    public void getMethod() {
        Assert.assertNull(klass.getMethod("get"));
        Assert.assertNotNull(klass.getMethod("get", Object.class));
        Assert.assertNotNull(klass.getMethod("wait"));
    }
}
