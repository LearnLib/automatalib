package net.automatalib.commons.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ReflectUtilTest {

    private final static Class<?>[] signature1 =
            new Class[] {Integer.class, Boolean.class, Character.class, Byte.class};
    private final static Class<?>[] signature2 = new Class[] {int.class, boolean.class, char.class, byte.class};
    private final static Class<?>[] signature3 = new Class[] {Integer.class, Boolean.class, char.class, byte.class};
    private final static Class<?>[] signature4 = new Class[] {Integer.class, byte.class};

    private Method methodToFind;
    private String methodName;

    @BeforeMethod
    public void setUp() throws NoSuchMethodException {
        methodToFind = TestClass.class.getDeclaredMethod("method1", signature3);
        methodName = methodToFind.getName();

    }

    @Test
    public void testConstructors() {

        @SuppressWarnings("unchecked")
        final Constructor<TestClass> actualConstructor = (Constructor<TestClass>) TestClass.class.getConstructors()[0];

        final Constructor<TestClass> constructor1 = ReflectUtil.findConstructor(TestClass.class, signature1);
        final Constructor<TestClass> constructor2 = ReflectUtil.findConstructor(TestClass.class, signature2);
        final Constructor<TestClass> constructor3 = ReflectUtil.findConstructor(TestClass.class, signature3);
        final Constructor<TestClass> constructor4 = ReflectUtil.findConstructor(TestClass.class, signature4);

        Assert.assertEquals(constructor1, actualConstructor);
        Assert.assertEquals(constructor2, actualConstructor);
        Assert.assertEquals(constructor3, actualConstructor);
        Assert.assertNull(constructor4);
    }

    @Test
    public void testMethod() {

        final Method method1 = ReflectUtil.findMethod(TestClass.class, methodName, signature1);
        final Method method2 = ReflectUtil.findMethod(TestClass.class, methodName, signature2);
        final Method method3 = ReflectUtil.findMethod(TestClass.class, methodName, signature3);
        final Method method4 = ReflectUtil.findMethod(TestClass.class, methodName, signature4);
        final Method method5 = ReflectUtil.findMethod(TestClass.class, "randomName", signature1);

        Assert.assertEquals(method1, methodToFind);
        Assert.assertEquals(method2, methodToFind);
        Assert.assertEquals(method3, methodToFind);
        Assert.assertNull(method4);
        Assert.assertNull(method5);
    }

    @Test
    public void testMethodByArgs() {

        final Method method1 =
                ReflectUtil.findMatchingMethod(TestClass.class, methodName, 1, Boolean.TRUE, 'c', (byte) 3);
        final Method method2 = ReflectUtil.findMatchingMethod(TestClass.class, methodName, 1, null, 'c', (byte) 3);
        final Method method3 = ReflectUtil.findMatchingMethod(TestClass.class, methodName, 1, true, null, null);
        final Method method4 = ReflectUtil.findMatchingMethod(TestClass.class, methodName, 4L, false);
        final Method method5 = ReflectUtil.findMatchingMethod(TestClass.class, "randomName", 1, true, 'c', (byte) 3);

        Assert.assertEquals(method1, methodToFind);
        Assert.assertEquals(method2, methodToFind);
        Assert.assertNull(method3);
        Assert.assertNull(method4);
        Assert.assertNull(method5);
    }

    @Test
    public void testMethodWithReturnType() {

        final Method method1 = ReflectUtil.findMethodRT(TestClass.class, methodName, null, signature1);
        final Method method2 = ReflectUtil.findMethodRT(TestClass.class, methodName, Void.class, signature1);
        final Method method3 = ReflectUtil.findMethodRT(TestClass.class, methodName, Integer.class, signature1);

        Assert.assertEquals(method1, methodToFind);
        Assert.assertEquals(method2, methodToFind);
        Assert.assertNull(method3);

        final Method method4 = ReflectUtil.findMethodRT(TestClass.class, methodName, null, signature2);
        final Method method5 = ReflectUtil.findMethodRT(TestClass.class, methodName, Void.class, signature2);
        final Method method6 = ReflectUtil.findMethodRT(TestClass.class, methodName, Integer.class, signature2);

        Assert.assertEquals(method4, methodToFind);
        Assert.assertEquals(method5, methodToFind);
        Assert.assertNull(method6);

        final Method method7 = ReflectUtil.findMethodRT(TestClass.class, methodName, null, signature4);
        final Method method8 = ReflectUtil.findMethodRT(TestClass.class, methodName, Void.class, signature4);
        final Method method9 = ReflectUtil.findMethodRT(TestClass.class, methodName, Integer.class, signature4);

        Assert.assertNull(method7);
        Assert.assertNull(method8);
        Assert.assertNull(method9);

        final Method method10 = ReflectUtil.findMethodRT(TestClass.class, "randomName", null, signature1);
        final Method method11 = ReflectUtil.findMethodRT(TestClass.class, "randomName", Void.class, signature1);
        final Method method12 = ReflectUtil.findMethodRT(TestClass.class, "randomName", Integer.class, signature1);

        Assert.assertNull(method10);
        Assert.assertNull(method11);
        Assert.assertNull(method12);
    }

    private final static class TestClass {

        public TestClass(Integer boxed1, Boolean boxed2, char primitive1, byte primitive2) {}

        public void method1(Integer boxed1, Boolean boxed2, char primitive1, byte primitive2) {}

    }
}
