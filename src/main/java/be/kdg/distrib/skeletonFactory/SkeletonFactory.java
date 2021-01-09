package be.kdg.distrib.skeletonFactory;

import java.lang.reflect.Proxy;

public class SkeletonFactory {
    public static Object createSkeleton(Object c){
        return Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[]{Skeleton.class}, new SkeletonInvocationHandler(c)
        );
    }
}
