package jetbrick.reflect.asm;

import java.text.SimpleDateFormat;
import java.util.*;
import jetbrick.reflect.FieldInfo;
import jetbrick.reflect.KlassInfo;
import org.junit.*;

public class ASMFactoryTest {
    @Before
    public void setup() {
        System.setProperty("jetbrick.asm.enabled", "true");
    }

    @After
    public void cleanup() {
        System.clearProperty("jetbrick.asm.enabled");
    }

    @Test
    public void generateConstructorAccessor() {
        ASMConstructorAccessor accessor = ASMFactory.generateConstructorAccessor(ArrayList.class);
        Assert.assertNotNull(accessor.newInstance());
        Assert.assertNotNull(accessor.newInstance(0, Arrays.asList()));
        Assert.assertNotNull(accessor.newInstance(1));
        Assert.assertNotNull(accessor.newInstance(2, 32));
    }

    @Test
    public void generateMethodAccessor() {
        List<Integer> list = Arrays.asList(11, 22, 33);

        KlassInfo klass = KlassInfo.create(List.class);
        ASMMethodAccessor accessor = ASMFactory.generateMethodAccessor(klass);
        Assert.assertEquals(list.size(), accessor.invoke(list, klass.getMethod("size").getOffset()));
        Assert.assertEquals(list.isEmpty(), accessor.invoke(list, klass.getMethod("isEmpty").getOffset()));
        Assert.assertEquals(list.get(1), accessor.invoke(list, klass.getMethod("get", int.class).getOffset(), 1));
    }

    @Test
    public void generateMethodAccessor2() {
        List<Integer> list = Arrays.asList(11, 22, 33);

        KlassInfo klass = KlassInfo.create(List.class);
        Assert.assertEquals(list.size(), klass.getMethod("size").invoke(list));
        Assert.assertEquals(list.isEmpty(), klass.getMethod("isEmpty").invoke(list));
        Assert.assertEquals(list.get(1), klass.getMethod("get", int.class).invoke(list, 1));
    }

    @Test
    public void testPrivateField() {
        String s = "abc";
        KlassInfo klass = KlassInfo.create(String.class);
        Assert.assertArrayEquals(s.toCharArray(), (char[]) klass.getField("value").get(s));
    }

    @Test
    public void testAsmGetFields() {
        SimpleDateFormat object = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        KlassInfo klass = KlassInfo.create(SimpleDateFormat.class);
        for (FieldInfo field : klass.getFields()) {
            field.get(object);
        }
    }

    @Test
    public void testReflectGetFields() throws Exception {
        SimpleDateFormat object = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        KlassInfo klass = KlassInfo.create(SimpleDateFormat.class);
        for (FieldInfo field : klass.getFields()) {
            field.getField().get(object);
        }
    }
}
