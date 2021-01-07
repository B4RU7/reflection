package be.kdg.distrib.stubFactory;

import java.lang.reflect.Proxy;

public class StubFactory {
    public static Object createStub(Class c, String arg0, int arg1 ){
        return Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[]{c}, new StubInvocationHandler(arg0,arg1)
        );
    }
}
