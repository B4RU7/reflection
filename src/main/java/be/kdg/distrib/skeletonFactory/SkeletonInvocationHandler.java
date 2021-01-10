package be.kdg.distrib.skeletonFactory;

import be.kdg.distrib.communication.MessageManager;
import be.kdg.distrib.communication.MethodCallMessage;
import be.kdg.distrib.communication.NetworkAddress;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

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
            return networkAddress;
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
                MethodCallMessage reply = new MethodCallMessage(networkAddress, "result");
                reply.setParameter("NotOk", e.getMessage());
                assert request != null;
                messageManager.send(reply, request.getOriginator());
            }
        }
    }

    public void handleRequest(MethodCallMessage message) throws Exception {
        String methodName = message.getMethodName();
        Set<String> paramNames = new HashSet<>();
        for (String key : message.getParameters().keySet()) {
            if (!key.startsWith("arg")){
                throw new RuntimeException("Parameter name must start with 'arg'!");
            }
            if (key.contains(".")) {
                paramNames.add(key.split("\\.")[0]);
            } else {
                paramNames.add(key);
            }
        }
        Optional<Method> method = Arrays.stream(c.getClass().getDeclaredMethods()).filter(e -> e.getName().equals(methodName)).findFirst();
        if (method.isPresent()) {
            if (paramNames.size() == method.get().getParameterCount()) {
                Object[] arguments = new Object[paramNames.size()];
                Class[] classes = method.get().getParameterTypes();
                for (int i = 0; i < paramNames.size(); i++) {
                    Class clazz = classes[i];
                    switch (clazz.getSimpleName()) {
                        case "String": {
                            arguments[i] = message.getParameter("arg" + i);
                            break;
                        }
                        case "int": {
                            arguments[i] = Integer.parseInt(message.getParameter("arg" + i));
                            break;
                        }
                        case "double":{
                            arguments[i] = Double.parseDouble(message.getParameter("arg" + i));
                            break;
                        }
                        case "boolean": {
                            arguments[i] = Boolean.parseBoolean(message.getParameter("arg" + i));
                            break;
                        }
                        case "char": {
                            arguments[i] = message.getParameter("arg" + i).charAt(0);
                            break;
                        }
                        default: {
                            Object object = clazz.getDeclaredConstructor().newInstance();
                            for (Field field : clazz.getDeclaredFields()) {
                                field.setAccessible(true);
                                switch (field.getType().getSimpleName()) {
                                    case "String": {
                                        field.set(object, message.getParameter("arg" + i + "." + field.getName()));
                                        break;
                                    }
                                    case "char": {
                                        field.set(object, message.getParameter("arg" + i + "." + field.getName()).charAt(0));
                                        break;
                                    }
                                    case "boolean": {
                                        field.set(object, Boolean.parseBoolean(message.getParameter("arg" + i + "." + field.getName())));
                                        break;
                                    }
                                    case "int": {
                                        field.set(object, Integer.parseInt(message.getParameter("arg" + i + "." + field.getName())));
                                        break;
                                    }
                                }
                                arguments[i] = object;
                            }
                        }
                    }
                }
                Object toReturn = method.get().invoke(c, arguments);
                Class clazz = method.get().getReturnType();
                MethodCallMessage reply = new MethodCallMessage(networkAddress, methodName);
                if ("void".equals(clazz.getSimpleName())) {
                    reply.setParameter("result", "Ok");
                } else if (clazz.isPrimitive() || clazz == String.class) {
                    reply.setParameter("result", toReturn.toString());
                } else {
                    for (Field field : clazz.getDeclaredFields()
                    ) {
                        field.setAccessible(true);
                        if (field.getType().isPrimitive() || field.getType() == String.class) {
                            reply.setParameter("result" + "." + field.getName(), field.get(toReturn).toString());
                        }
                    }
                }
                messageManager.send(reply, message.getOriginator());
            } else {
                throw new RuntimeException("Error in param!");
            }
        } else {
            throw new RuntimeException("Method not found");
        }

    }
}
