package be.kdg.distrib.skeletonFactory;

import be.kdg.distrib.communication.MessageManager;
import be.kdg.distrib.communication.MethodCallMessage;
import be.kdg.distrib.communication.NetworkAddress;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

public class SkeletonInvocationHandler implements InvocationHandler{
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
        if ("run".equals(method.getName())) {
            new Thread(this::run).start();
        } else if ("getAddress".equals(method.getName())) {
            return messageManager.getMyAddress();
        } else if ("handleRequest".equals(method.getName())) {
            handleRequest((MethodCallMessage) args[0]);
        }
        return c;
    }

    private void run(){
        while (true) {
            MethodCallMessage request = null;
            try {
                request = messageManager.wReceive();
                handleRequest(request);
            } catch (Exception e) {
                MethodCallMessage reply = new MethodCallMessage(messageManager.getMyAddress(), "result");
                reply.setParameter("NotOk", e.getMessage());
                assert request != null;
                messageManager.send(reply, request.getOriginator());
            }
        }
    }

    public void handleRequest(MethodCallMessage message) throws InvocationTargetException, IllegalAccessException {
        String methodName = message.getMethodName();
        HashSet<String> paramNames = new HashSet<>();
        for (String key : message.getParameters().keySet()) {
            paramNames.add(key);
        }
        Optional<Method> method = Arrays.stream(c.getClass().getDeclaredMethods()).filter(e -> e.getName().equals(methodName)).findFirst();
        if (method.isPresent()){
            for (Method m : c.getClass().getDeclaredMethods()) {
                m.setAccessible(true);
                if (m.getName().equals(methodName)){
                    if (paramNames.size() == m.getParameterCount()){
                        Object[] arguments = new Object[paramNames.size()];
                        Class[] clazzes = m.getParameterTypes();
                        for (int i = 0; i < paramNames.size(); i++) {
                            Class clazz = clazzes[i];
                            switch (clazz.getSimpleName()){
                                // case ""
                            }
                        }
                        Object toReturn = m.invoke(c, arguments);
                        Class clazz = m.getReturnType();
                        MethodCallMessage reply = new MethodCallMessage(messageManager.getMyAddress(), methodName);
                        if ("void".equals(clazz.getSimpleName())) {
                            reply.setParameter("result", "Ok");
                        } else if (clazz.isPrimitive() || clazz == String.class) {
                            reply.setParameter("result", toReturn.toString());
                        }
                        messageManager.send(reply, message.getOriginator());
                    } else {
                        throw new RuntimeException("Error in parameters!");
                    }
                }
            }
        } else {
            throw new RuntimeException("Method " + method.get().getName() + " doesn't exist!");
        }

    }
}
