package be.kdg.distrib.stubFactory;

import be.kdg.distrib.communication.MessageManager;
import be.kdg.distrib.communication.MethodCallMessage;
import be.kdg.distrib.communication.NetworkAddress;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class StubInvocationHandler implements InvocationHandler {
    private String arg0;
    private int arg1;
    private final NetworkAddress networkAddress;
    private final MessageManager messageManager;

    public StubInvocationHandler(String arg0, int arg1) {
        this.arg0 = arg0;
        this.arg1 = arg1;
        this.networkAddress = new NetworkAddress(arg0,arg1);
        this.messageManager = new MessageManager();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodCallMessage message = new MethodCallMessage(messageManager.getMyAddress(),method.getName());
        messageManager.send(message,networkAddress);
        return null;
    }
}
