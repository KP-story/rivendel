package com.kp.pojo;


import com.kp.common.utilities.StringUtils;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;

public abstract class ClassUtils {
    static public ParameterizedType getParameterizedType(Class<?> target) {
        Type[] types = getGenericType(target);
        if (types.length > 0 && types[0] instanceof ParameterizedType) {
            return (ParameterizedType) types[0];
        }
        return null;
    }

    public static Class getWrapPrimitiveType(Class fieldType) {
        if (fieldType == String.class) {
            return String.class;
        } else if (fieldType == Integer.TYPE || fieldType == Integer.class) {
            return Integer.class;
        } else if (fieldType == Float.TYPE || fieldType == Float.class) {
            return Float.class;

        } else if (fieldType == Long.class || fieldType == Long.TYPE) {
            return Long.class;
        } else if (fieldType == Double.TYPE || fieldType == Double.class) {
            return Double.class;
        } else if (fieldType == Short.TYPE || fieldType == Short.class) {
            return Short.class;

        } else if (fieldType == Byte.TYPE || fieldType == Byte.class) {
            return Byte.class;

        } else if (fieldType == BigDecimal.class) {
            return Byte.class;
        } else if (fieldType == Boolean.TYPE || fieldType == Boolean.class) {
            return Boolean.class;

        } else {
            return fieldType;
        }
    }

    public static List<SetterMethodSignature> extractSetterMethodSignatures(Class<?> targetType) {
        List results = new LinkedList<SetterMethodSignature>();
        Collection<Method> methods = extractAllMethods(targetType);

        for (Method method : methods) {
            String methodName = method.getName();
            method.getGenericParameterTypes();
            if (method.getParameterCount() == 1 //
                    && Modifier.isPublic(method.getModifiers()) //
                    && method.getReturnType() == Void.TYPE //
                    && methodName.startsWith("set")) {

                if (methodName.length() <= 3)
                    continue;
                String fieldName = StringUtils.lowerCaseFirstLetter(methodName.substring(3));
                Parameter param = method.getParameters()[0];
                Class<?> paramType = param.getType();

                results.add(new SetterMethodSignature(fieldName, methodName, paramType, method.getGenericParameterTypes(), method.getAnnotations()
                ));

            }
        }
        return results;
    }

    public static ClassInfo extractInfo(Class<?> targetType, Type type,boolean findAttribute) {
        Collection<Method> methods = extractAllMethods(targetType);
        ClassInfo classInfo = new ClassInfo();
        classInfo.setName(targetType.getName());
        classInfo.setSimpleName(targetType.getSimpleName());
        classInfo.setType(targetType, type);
        List<ClassInfo.AttributeInfo> attributes = new LinkedList<>();

        if(findAttribute){
        for (Method method : methods) {
            String methodName = method.getName();
            method.getGenericParameterTypes();
            if (method.getParameterCount() == 0//
                    && Modifier.isPublic(method.getModifiers()) //

                    && methodName.startsWith("get")) {

                if (methodName.length() <= 3)
                    continue;
                String fieldName = StringUtils.lowerCaseFirstLetter(methodName.substring(3));
                ClassInfo attribute = extractInfo(method.getReturnType(), method.getGenericReturnType(),false);
                ClassInfo.AttributeInfo attributeInfo = new ClassInfo.AttributeInfo();
                attributeInfo.setAttrName(fieldName);
                attributeInfo.setClassInfo(attribute);
                attributes.add(attributeInfo);


            }
        }
        }
        classInfo.setAttributes(attributes);
        return classInfo;
    }


    private static Collection<Method> extractAllMethods(Class<?> targetType) {
        Map<String, Method> nameToMethod = new HashMap<String, Method>();
        Class<?> t = targetType;
        do {
            Method[] methods = t.getDeclaredMethods();
            for (Method method : methods) {
                if (!nameToMethod.containsKey(method.getName())) {
                    nameToMethod.put(method.getName(), method);
                }
            }
            t = t.getSuperclass();
        } while (t != null && t != Object.class);
        return nameToMethod.values();
    }

    private static Collection<Constructor> extractAllConstructions(Class<?> targetType) {
        Map<String, Constructor> nameToMethod = new HashMap<String, Constructor>();
        Class<?> t = targetType;
        Constructor[] methods = t.getConstructors();
        for (Constructor method : methods) {
            if (!nameToMethod.containsKey(method.getName())) {
                nameToMethod.put(method.getName(), method);
            }
        }

        return nameToMethod.values();
    }

    private static boolean hasConstructorNoParam(Class<?> targetType) {
        Constructor[] methods = targetType.getDeclaredConstructors();
        for (Constructor method : methods) {
            System.out.println(method.getParameterCount());
            System.out.println(method.getParameters());

            if (method.getParameters() == null || method.getParameters().length == 0) {
                return true;
            }
        }
        return false;
    }

    static public Type[] getParameterizedTypes(Class<?> target) {
        Type[] types = getGenericType(target);
        if (types.length > 0 && types[0] instanceof ParameterizedType) {
            return ((ParameterizedType) types[0]).getActualTypeArguments();
        }
        return null;
    }

    static public Type[] getGenericType(Class<?> target) {
        if (target == null)
            return new Type[0];
        Type[] types = target.getGenericInterfaces();
        if (types.length > 0) {
            return types;
        }
        Type type = target.getGenericSuperclass();
        if (type != null) {
            if (type instanceof ParameterizedType) {
                return new Type[]{type};
            }
        }
        return new Type[0];
    }

}
