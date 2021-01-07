package be.kdg.distrib.stubFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class StubInvocationHandler implements InvocationHandler {
    private String arg0;
    private int arg1;

    public StubInvocationHandler(String arg0, int arg1 ) {
        this.arg0 = arg0;
        this.arg1 = arg1;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }
}
