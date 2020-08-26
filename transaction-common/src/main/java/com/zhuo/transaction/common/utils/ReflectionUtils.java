package com.zhuo.transaction.common.utils;

import javax.naming.Context;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/19
 */
public class ReflectionUtils {


    public static void makeAccessible(Method method) {
        if ((!Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method.getDeclaringClass().getModifiers())) && !method.isAccessible()) {
            method.setAccessible(true);
        }
    }
    public static Object changeAnnotationValue(Annotation annotation, String key, Object newValue) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Object handler = Proxy.getInvocationHandler(annotation);
        Field f;
        f = handler.getClass().getDeclaredField("memberValues");
        f.setAccessible(true);
        Map<String, Object> memberValues;
        memberValues = (Map<String, Object>) f.get(handler);
        Object oldValue = memberValues.get(key);
        if (oldValue == null || oldValue.getClass() != newValue.getClass()) {
            throw new IllegalArgumentException();
        }
        memberValues.put(key, newValue);
        return oldValue;
    }

    public static Class getDeclaringType(Class aClass, String methodName, Class<?>[] parameterTypes) {
        Method method = null;
        Class findClass = aClass;
        do {
            Class[] clazzes = findClass.getInterfaces();
            for (Class clazz : clazzes) {
                try {
                    method = clazz.getDeclaredMethod(methodName, parameterTypes);
                } catch (NoSuchMethodException e) {
                    method = null;
                }
                if (method != null) {
                    return clazz;
                }
            }
            findClass = findClass.getSuperclass();
        } while (!findClass.equals(Object.class));

        return aClass;
    }

    public static Object getNullValue(Class type) {
        if (boolean.class.equals(type)) {
            return false;
        } else if (byte.class.equals(type)) {
            return 0;
        } else if (short.class.equals(type)) {
            return 0;
        } else if (int.class.equals(type)) {
            return 0;
        } else if (long.class.equals(type)) {
            return 0;
        } else if (float.class.equals(type)) {
            return 0;
        } else if (double.class.equals(type)) {
            return 0;
        } else if (char.class.equals(type)){
            return ' ';
        }
        return null;
    }




    /**
     * 获取指定路径下的所有类名
     * @param url
     * @return
     */
    public static List<String> getClassesList(String url) {
        File file = new File(url);
        List<String> classes = getAllClass(file);
        for (int i = 0; i < classes.size(); i++) {
            classes.set(i, classes.get(i).replace(url, "").replace(".class", "").replace("\\", "."));
        }
        return classes;
    }
    public static String getClassPath(String basePackage) throws Exception {
        String packageDirName = basePackage.replace('.', '/');
        URL resource = Context.class.getResource(packageDirName);
        String url = URLDecoder.decode(Context.class.getResource(packageDirName).getPath(), Charset.defaultCharset().name());
        if (url.startsWith("/")) {
            url = url.replaceFirst("/", "");
        }
        url = url.replaceAll("/", "\\\\");
        return url;
    }
    public static String getClassPath() throws Exception {
        String url = URLDecoder.decode(Context.class.getResource("/").getPath(), Charset.defaultCharset().name());
        if (url.startsWith("/")) {
            url = url.replaceFirst("/", "");
        }
        url = url.replaceAll("/", "\\\\");
        return url;
    }
    private static List<String> getAllClass(File file) {
        List<String> ret = new ArrayList<>();
        if (file.isDirectory()) {
            File[] list = file.listFiles();
            for (File i : list) {
                List<String> j = getAllClass(i);
                ret.addAll(j);
            }
        } else {
            ret.add(file.getAbsolutePath());
        }
        return ret;
    }

    /**
     *  判断类中方法是否包含某个注解
     * @param className
     * @param annotation
     * @return
     */
    public static List<String> gethasAnnotationMethod(String className,Class<? extends Annotation> annotation){
        List<String> methodPathList = new ArrayList<>();
        try {
            Class<?> clazz = Class.forName(className);
            Method[] methods = clazz.getMethods();
            for(Method method : methods){
                if(method.getAnnotation(annotation) != null){
                    methodPathList.add(className+"."+method.getName());
                }
            }
        }catch (Exception e){
        }
        return methodPathList;
    }
}
