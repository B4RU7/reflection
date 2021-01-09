package be.kdg.distrib.stubFactory;

import be.kdg.distrib.communication.MessageManager;
import be.kdg.distrib.communication.MethodCallMessage;
import be.kdg.distrib.communication.NetworkAddress;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

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

//        if (args.length == 1){
////            if (!args[0].getClass().getSimpleName().equals(String.class.getSimpleName())){
////                Class o = (args[0]).getClass();
////                o.newInstance();
////                o.
////
////            }
//
//        }
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


//        for (Object arg : args) {
//            System.out.println("\targ" + + argCounter + " = " + arg);
//            message.setParameter("arg" + argCounter,arg.toString());
//            argCounter++;
//        }
        messageManager.send(message,networkAddress);
        return null;
    }
}
