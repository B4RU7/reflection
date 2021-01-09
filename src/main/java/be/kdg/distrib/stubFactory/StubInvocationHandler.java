package be.kdg.distrib.stubFactory;

import be.kdg.distrib.communication.MessageManager;
import be.kdg.distrib.communication.MethodCallMessage;
import be.kdg.distrib.communication.NetworkAddress;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class StubInvocationHandler implements InvocationHandler {
    private final NetworkAddress networkAddress;
    private final MessageManager messageManager;

    public StubInvocationHandler(String arg0, int arg1) {
        this.networkAddress = new NetworkAddress(arg0,arg1);
        this.messageManager = new MessageManager();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodCallMessage message = new MethodCallMessage(messageManager.getMyAddress(),method.getName());
        int argCounter = 0;
        for (Class clazz :method.getParameterTypes()
             ) {
            if (clazz.isPrimitive() || clazz == String.class){
                message.setParameter("arg"+argCounter,args[argCounter].toString());
                argCounter++;
            } else {
                for (Field field : clazz.getDeclaredFields()
                     ) {
                    field.setAccessible(true);
                    if (field.getType().isPrimitive() || field.getType() == String.class){
                        message.setParameter("arg"+argCounter + "." + field.getName(),field.get(args[argCounter]).toString());
                    }
                }
                argCounter++;
            }
        }
        messageManager.send(message,networkAddress);
        MethodCallMessage reply = messageManager.wReceive();
        switch (method.getReturnType().getSimpleName()){
            case "void" : {
                if (reply.getParameter("result").equals("Ok")){
                    return null;
                } else {
                    System.out.println("Void result is not: Ok");
                }
            }
            case "String" : {
                return reply.getParameter("result");
            }
            case "char" : {
                return reply.getParameter("result").charAt(0);
            }
            case "boolean" : {
                return Boolean.parseBoolean(reply.getParameter("result"));
            }
            case "int" : {
                return Integer.parseInt(reply.getParameter("result"));
            }
            default: {
                Object object = method.getReturnType().getDeclaredConstructor().newInstance();
                for (Field field :
                        method.getReturnType().getDeclaredFields()) {
                    field.setAccessible(true);
                    parsePrimitiveType(field,reply,object);
                }
                return object;
            }
        }
    }

    private void parsePrimitiveType(Field field,MethodCallMessage reply, Object object) throws IllegalAccessException {
        switch (field.getType().getSimpleName()){
            case "String": {
                field.set(object,reply.getParameter("result." + field.getName()));
                break;
            }
            case "char": {
                field.set(object,reply.getParameter("result." + field.getName()).charAt(0));
                break;
            }
            case "int": {
                field.set(object,Integer.parseInt(reply.getParameter("result." + field.getName())));
                break;
            }
            case "boolean": {
                field.set(object,Boolean.parseBoolean(reply.getParameter("result." + field.getName())));
                break;
            }
        }
    }
}
