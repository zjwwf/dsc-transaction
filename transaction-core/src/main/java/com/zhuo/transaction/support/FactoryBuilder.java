package com.zhuo.transaction.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/18
 */
public final class FactoryBuilder {


    private FactoryBuilder() {

    }

    private static List<BeanFactory> beanFactories = new ArrayList<BeanFactory>();

    private static ConcurrentHashMap<Class, SingeltonFactory> classFactoryMap = new ConcurrentHashMap<Class, SingeltonFactory>();

    /**
     * clazz 有可能是一个接口父类，当为clazz为接口父类是使用 realClass
     * @param clazz
     * @param realClass
     * @param <T>
     * @return
     */
    public static <T> SingeltonFactory<T> factoryOf(Class<T> clazz,Class<T> realClass) {

        if (!classFactoryMap.containsKey(clazz)) {

            for (BeanFactory beanFactory : beanFactories) {
                if (beanFactory.isFactoryOf(clazz)) {
                    classFactoryMap.putIfAbsent(clazz, new SingeltonFactory<T>(clazz, beanFactory.getBean(clazz)));
                }
            }

            if (!classFactoryMap.containsKey(clazz)) {
                if(clazz.isInterface()){
                    classFactoryMap.putIfAbsent(clazz, new SingeltonFactory<T>(realClass));
                }else{
                    classFactoryMap.putIfAbsent(clazz, new SingeltonFactory<T>(clazz));
                }
            }
        }

        return classFactoryMap.get(clazz);
    }

    public static void registerBeanFactory(BeanFactory beanFactory) {
        beanFactories.add(beanFactory);
    }

    public static class SingeltonFactory<T> {

        private volatile T instance = null;

        private String className;

        public SingeltonFactory(Class<T> clazz, T instance) {
            this.className = clazz.getName();
            this.instance = instance;
        }

        public SingeltonFactory(Class<T> clazz) {
            this.className = clazz.getName();
        }

        public T getInstance() {

            if (instance == null) {
                synchronized (SingeltonFactory.class) {
                    if (instance == null) {
                        try {
                            ClassLoader loader = Thread.currentThread().getContextClassLoader();

                            Class<?> clazz = loader.loadClass(className);

                            instance = (T) clazz.newInstance();
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to create an instance of " + className, e);
                        }
                    }
                }
            }

            return instance;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other){
                return true;
            }
            if (other == null || getClass() != other.getClass()){
                return false;
            }
            SingeltonFactory that = (SingeltonFactory) other;
            if (!className.equals(that.className)){
                return false;
            }
            return true;
        }
        @Override
        public int hashCode() {
            return className.hashCode();
        }
    }
}