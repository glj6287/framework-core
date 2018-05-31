package com.hywin.framework.utils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by wuyouyang on 2017/4/27.
 */
public class ReflectUtils
{
    private static Logger logger = LogManager.getLogger(ReflectUtils.class);
    private static final String SETTER_PREFIX = "set";
    private static final String GETTER_PREFIX = "get";
    private static final String CGLIB_CLASS_SEPARATOR = "$$";

    public static Object invokeGetter(Object obj, String propertyName)
    {
        Object object = obj;
        for (String name : StringUtils.split(propertyName, ".")) {
            String getterMethodName = "get" + StringUtils.capitalize(name);
            object = invokeMethod(object, getterMethodName, new Class[0], new Object[0]);
        }
        return object;
    }

    public static void invokeSetter(Object obj, String propertyName, Object value)
    {
        Object object = obj;
        String[] names = StringUtils.split(propertyName, ".");
        for (int i = 0; i < names.length; i++)
            if (i < names.length - 1) {
                String getterMethodName = "get" + StringUtils.capitalize(names[i]);
                object = invokeMethod(object, getterMethodName, new Class[0], new Object[0]);
            } else {
                String setterMethodName = "set" + StringUtils.capitalize(names[i]);
                invokeMethodByName(object, setterMethodName, new Object[] { value });
            }
    }

    public static Object getFieldValue(Object obj, String fieldName)
    {
        Field field = getAccessibleField(obj, fieldName);

        if (field == null) {
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + obj + "]");
        }

        Object result = null;
        try {
            result = field.get(obj);
        } catch (IllegalAccessException e) {
            logger.error("不可能抛出的异常{}", e.getMessage());
        }
        return result;
    }

    public static void setFieldValue(Object obj, String fieldName, Object value)
    {
        Field field = getAccessibleField(obj, fieldName);

        if (field == null) {
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + obj + "]");
        }
        try
        {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            logger.error("不可能抛出的异常:{}", e.getMessage());
        }
    }

    public static Object invokeMethod(Object obj, String methodName, Class<?>[] parameterTypes, Object[] args)
    {
        Method method = getAccessibleMethod(obj, methodName, parameterTypes);
        if (method == null) {
            throw new IllegalArgumentException("Could not find method [" + methodName + "] on target [" + obj + "]");
        }
        try
        {
            return method.invoke(obj, args);
        } catch (Exception e) {
            throw convertReflectionExceptionToUnchecked(e);
        }
    }

    public static Object invokeMethodByName(Object obj, String methodName, Object[] args)
    {
        Method method = getAccessibleMethodByName(obj, methodName);
        if (method == null) {
            throw new IllegalArgumentException("Could not find method [" + methodName + "] on target [" + obj + "]");
        }
        try
        {
            return method.invoke(obj, args);
        }
        catch (Exception e) {
            throw convertReflectionExceptionToUnchecked(e);
        }

    }

    public static Field getAccessibleField(Object obj, String fieldName)
    {
        Validate.notNull(obj, "object can't be null", new Object[0]);
        Validate.notBlank(fieldName, "fieldName can't be blank", new Object[0]);
        for (Class superClass = obj.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                Field field = superClass.getDeclaredField(fieldName);
                makeAccessible(field);
                return field;
            }
            catch (NoSuchFieldException e)
            {
            }
        }
        return null;
    }

    public static Method getAccessibleMethod(Object obj, String methodName, Class<?>[] parameterTypes)
    {
        Validate.notNull(obj, "object can't be null", new Object[0]);
        Validate.notBlank(methodName, "methodName can't be blank", new Object[0]);

        for (Class searchType = obj.getClass(); searchType != Object.class; searchType = searchType.getSuperclass()) {
            try {
                Method method = searchType.getDeclaredMethod(methodName, parameterTypes);
                makeAccessible(method);
                return method;
            }
            catch (NoSuchMethodException e)
            {
            }
        }
        return null;
    }

    public static Method getAccessibleMethodByName(Object obj, String methodName)
    {
        Validate.notNull(obj, "object can't be null", new Object[0]);
        Validate.notBlank(methodName, "methodName can't be blank", new Object[0]);

        for (Class searchType = obj.getClass(); searchType != Object.class; searchType = searchType.getSuperclass()) {
            Method[] methods = searchType.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    makeAccessible(method);
                    return method;
                }
            }
        }
        return null;
    }

    public static void makeAccessible(Method method)
    {
        if (((!Modifier.isPublic(method.getModifiers())) || (!Modifier.isPublic(method.getDeclaringClass().getModifiers()))) &&
                (!method
                        .isAccessible()))
            method.setAccessible(true);
    }

    public static void makeAccessible(Field field)
    {
        if (((!Modifier.isPublic(field.getModifiers())) || (!Modifier.isPublic(field.getDeclaringClass().getModifiers())) ||
                (Modifier.isFinal(field
                        .getModifiers()))) && (!field.isAccessible()))
            field.setAccessible(true);
    }

    public static <T> Class<T> getClassGenricType(Class clazz)
    {
        return getClassGenricType(clazz, 0);
    }

    public static Class getClassGenricType(Class clazz, int index)
    {
        Type genType = clazz.getGenericSuperclass();

        if (!(genType instanceof ParameterizedType)) {
            logger.warn(clazz.getSimpleName() + "'s superclass not ParameterizedType");
            return Object.class;
        }

        Type[] params = ((ParameterizedType)genType).getActualTypeArguments();

        if ((index >= params.length) || (index < 0)) {
            logger.info("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: " + params.length);

            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            logger.warn(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
            return Object.class;
        }

        return (Class)params[index];
    }

    public static Class<?> getUserClass(Object instance) {
        Validate.notNull(instance, "Instance must not be null", new Object[0]);
        Class clazz = instance.getClass();
        if ((clazz != null) && (clazz.getName().contains("$$"))) {
            Class superClass = clazz.getSuperclass();
            if ((superClass != null) && (!Object.class.equals(superClass))) {
                return superClass;
            }
        }
        return clazz;
    }

    public static RuntimeException convertReflectionExceptionToUnchecked(Exception e)
    {
        if (((e instanceof IllegalAccessException)) || ((e instanceof IllegalArgumentException)) || ((e instanceof NoSuchMethodException)))
        {
            return new IllegalArgumentException(e);
        }if ((e instanceof InvocationTargetException))
        return new RuntimeException(((InvocationTargetException)e).getTargetException());
        if ((e instanceof RuntimeException)) {
            return (RuntimeException)e;
        }
        return new RuntimeException("Unexpected Checked Exception.", e);
    }

    public static <T> boolean isDateType(Class<T> clazz, String fieldName)
    {
        boolean flag = false;
        try {
            Field field = clazz.getDeclaredField(fieldName);
            Object typeObj = field.getType().newInstance();
            flag = typeObj instanceof Date;
        }
        catch (Exception localException) {
        }
        return flag;
    }

    public static <T> Object parseValueWithType(String value, Class<?> type)
    {
        Object result = null;
        try {
            if (Boolean.TYPE == type)
                result = Boolean.valueOf(Boolean.parseBoolean(value));
            else if (Byte.TYPE == type)
                result = Byte.valueOf(Byte.parseByte(value));
            else if (Short.TYPE == type)
                result = Short.valueOf(Short.parseShort(value));
            else if (Integer.TYPE == type)
                result = Integer.valueOf(Integer.parseInt(value));
            else if (Long.TYPE == type)
                result = Long.valueOf(Long.parseLong(value));
            else if (Float.TYPE == type)
                result = Float.valueOf(Float.parseFloat(value));
            else if (Double.TYPE == type)
                result = Double.valueOf(Double.parseDouble(value));
            else
                result = value;
        }
        catch (Exception localException)
        {
        }
        return result;
    }

    public static PropertyDescriptor[] getPropertyDescriptors(Class<?> type)
    {
        return org.springframework.cglib.core.ReflectUtils.getBeanSetters(type);
    }

    public static Method getBeanGetter(Class<?> type, String property) throws SecurityException, NoSuchMethodException {
        String methodName = null;
        if (property.length() == 1)
            methodName = property.substring(0, 1).toUpperCase();
        else {
            methodName = property.substring(0, 1).toUpperCase() + property.substring(1, property.length());
        }
        methodName = "get" + methodName;
        return type.getMethod(methodName, new Class[0]);
    }

    public static Field getFieldByGetter(Class<?> modelClass, String getterName) throws NoSuchFieldException {
        String propName = StringUtils.uncapitalize(getterName.substring(3));
        return modelClass.getDeclaredField(propName);
    }

    public static Set<Field> getAllFields(Object ojb)
    {
        Set sourceSet = new HashSet();
        for (Class clazz = ojb.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            sourceSet.addAll(Arrays.asList(clazz.getDeclaredFields()));
        }
        return sourceSet;
    }

    public static Object getFieldValue(Object obj, Field f)
    {
        if (f == null) {
            return null;
        }
        boolean access = f.isAccessible();
        f.setAccessible(true);
        Object value = null;
        try {
            value = f.get(obj);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            f.setAccessible(access);
        }
        return value;
    }

    public static void setFieldValue(Object obj, Field f, Object value)
    {
        if (value == null) {
            return;
        }
        boolean access = f.isAccessible();
        f.setAccessible(true);
        try {
            f.set(obj, value);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            f.setAccessible(access);
        }
    }

    public static boolean isBeanPropertyReadMethod(Method method)
    {
        return (method != null) &&
                (Modifier.isPublic(method
                        .getModifiers())) &&
                (!Modifier.isStatic(method
                        .getModifiers())) &&
                (method
                        .getReturnType() != Void.TYPE) &&
                (method
                        .getDeclaringClass() != Object.class) &&
                (method
                        .getParameterTypes().length == 0) && (
                ((method
                        .getName().startsWith("get")) && (method.getName().length() > 3)) || (
                        (method
                                .getName().startsWith("is")) && (method.getName().length() > 2)));
    }

    public static String getPropertyNameFromBeanReadMethod(Method method) {
        if (isBeanPropertyReadMethod(method)) {
            if (method.getName().startsWith("get")) {
                return method.getName().substring(3, 4).toLowerCase() + method
                        .getName().substring(4);
            }
            if (method.getName().startsWith("is")) {
                return method.getName().substring(2, 3).toLowerCase() + method
                        .getName().substring(3);
            }
        }
        return null;
    }

    public static boolean isBeanPropertyWriteMethod(Method method) {
        return (method != null) &&
                (Modifier.isPublic(method
                        .getModifiers())) &&
                (!Modifier.isStatic(method
                        .getModifiers())) &&
                (method
                        .getDeclaringClass() != Object.class) &&
                (method
                        .getParameterTypes().length == 1) &&
                (method
                        .getName().startsWith("set")) &&
                (method
                        .getName().length() > 3);
    }

    public static String getPropertyNameFromBeanWriteMethod(Method method) {
        if (isBeanPropertyWriteMethod(method)) {
            return method.getName().substring(3, 4).toLowerCase() + method
                    .getName().substring(4);
        }
        return null;
    }

    public static boolean isPublicInstanceField(Field field) {
        return (Modifier.isPublic(field.getModifiers())) &&
                (!Modifier.isStatic(field
                        .getModifiers())) &&
                (!Modifier.isFinal(field
                        .getModifiers())) &&
                (!field
                        .isSynthetic());
    }

    public static List<String> getBeanPropertyFields(Class cl, boolean recur)
    {
        List properties = new ArrayList();
        if (recur) {
            for (; cl != null; cl = cl.getSuperclass()) {
                Field[] fields = cl.getDeclaredFields();
                for (Field field : fields) {
                    if ((Modifier.isTransient(field.getModifiers())) || (Modifier.isStatic(field.getModifiers()))) {
                        continue;
                    }
                    field.setAccessible(true);
                    properties.add(field.getName());
                }
            }
        }
        Field[] fields = cl.getDeclaredFields();
        for (Field field : fields) {
            if ((Modifier.isTransient(field.getModifiers())) || (Modifier.isStatic(field.getModifiers()))) {
                continue;
            }
            field.setAccessible(true);
            properties.add(field.getName());
        }

        return properties;
    }

    public static Map<String, Field> getBeanPropertyFieldsMap(Class cl, boolean recur)
    {
        Map properties = new HashMap();
        if (recur) {
            for (; cl != null; cl = cl.getSuperclass()) {
                Field[] fields = cl.getDeclaredFields();
                for (Field field : fields) {
                    if ((Modifier.isTransient(field.getModifiers())) || (Modifier.isStatic(field.getModifiers()))) {
                        continue;
                    }
                    field.setAccessible(true);
                    properties.put(field.getName(), field);
                }
            }
        }
        Field[] fields = cl.getDeclaredFields();
        for (Field field : fields) {
            if ((Modifier.isTransient(field.getModifiers())) || (Modifier.isStatic(field.getModifiers()))) {
                continue;
            }
            field.setAccessible(true);
            properties.put(field.getName(), field);
        }

        return properties;
    }

    public static Map<String, Method> getBeanPropertyReadMethods(Class cl)
    {
        Map properties = new HashMap();
        for (; cl != null; cl = cl.getSuperclass()) {
            Method[] methods = cl.getDeclaredMethods();
            for (Method method : methods) {
                if (isBeanPropertyReadMethod(method)) {
                    method.setAccessible(true);
                    String property = getPropertyNameFromBeanReadMethod(method);
                    properties.put(property, method);
                }
            }
        }
        return properties;
    }
}
