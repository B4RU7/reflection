package be.kdg.distrib.skeletonFactory;

import be.kdg.distrib.communication.MessageManager;
import be.kdg.distrib.communication.MethodCallMessage;
import be.kdg.distrib.communication.NetworkAddress;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SkeletonInvocationHandler implements InvocationHandler,Skeleton{
    private final MessageManager messageManager;
    private final Object c;
    private final NetworkAddress networkAddress;

    public SkeletonInvocationHandler(Object c) {
        this.messageManager = new MessageManager();
        this.networkAddress = messageManager.getMyAddress();
        this.c = c;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return c;
    }

    @Override
    public void run() {
        while (true) {
            MethodCallMessage message = messageManager.wReceive();
            handleRequest(message);
        }
    }

    @Override
    public NetworkAddress getAddress() {
        return networkAddress;
    }

    @Override
    public void handleRequest(MethodCallMessage message) {
        String methodName = message.getMethodName();
        int paramCount = message.getParameters().size();

        for (Method m : c.getClass().getDeclaredMethods()) {
            m.setAccessible(true);
            if (m.getName().equals(methodName)){
                try {
                    m.invoke(c);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
