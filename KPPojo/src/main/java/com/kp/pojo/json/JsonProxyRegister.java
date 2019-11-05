package com.kp.pojo.json;//package com.example.demo;

import com.kp.pojo.KType;
import com.kp.pojo.ProxyBuilderException;
import com.kp.pojo.SetterMethodSignature;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.reflections.Reflections;

import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class JsonProxyRegister {
    private static final Map<String, KPDeserializer> cache = new HashMap<>();
    private AtomicLong subFix = new AtomicLong(1);


    public static KPDeserializer getDeserialzer(String name) {
        return cache.get(name);
    }


    protected Class buildJsonDeserializerProxy(KType beanClass) {

        try {
            String className = beanClass.getJsonDeClassName();
            ClassPool pool = ClassPool.getDefault();
            pool.insertClassPath(new ClassClassPath(beanClass.getClass()));
            pool.importPackage("java.lang");
            pool.importPackage("com.kp.pojo");
            pool.importPackage("com.kp.pojo.json");

            CtClass cc = pool.makeClass(className);

            cc.defrost();
            cc.addInterface(pool.get(KPDeserializer.class.getName()));
            List<SetterMethodSignature> setterMethods = beanClass.extractSetter();
            StringBuilder methodBody = new StringBuilder();
            methodBody.append("public java.lang.Object");
//            methodBody.append(beanClass.getName());
            methodBody.append(" from(com.fasterxml.jackson.core.JsonParser jsonParser) throws  java.lang.Exception {\n");

            methodBody.append(beanClass.getName());
            methodBody.append(" bean= new ");
            methodBody.append(beanClass.getName());
            methodBody.append("();\n");
            methodBody.append(" while (jsonParser.nextToken() != com.fasterxml.jackson.core.JsonToken.END_OBJECT) { \n");
            methodBody.append(" String name = jsonParser.getCurrentName();\n");
            for (SetterMethodSignature setterMethod : setterMethods) {
                methodBody.append(" if(name.equals(\"");
                methodBody.append(setterMethod.fieldName);
                methodBody.append("\")){\n");
                methodBody.append(" jsonParser.nextToken();\n");
                KType kType = setterMethod.fieldType;
                StringBuilder parameter = new StringBuilder();
                fillParam(kType, methodBody, parameter);
                methodBody.append("bean.");
                methodBody.append(setterMethod.methodName);
                methodBody.append("(");
                methodBody.append(parameter);
                methodBody.append(");");
                methodBody.append("}else\n");
            }
            int index = methodBody.lastIndexOf("else");
            if (index == methodBody.length() - 5) {
                methodBody.delete(index, methodBody.length() - 1);

            }
            methodBody.append("} \n");

            methodBody.append("return bean; }\n");
            System.out.println(methodBody);
            cc.addMethod(CtMethod.make(methodBody.toString(), cc));

            Class<?> resultClass = cc.toClass();
            cache.put(beanClass.getName(), (KPDeserializer) resultClass.newInstance());
            return resultClass;
        } catch (Exception e) {
            throw new RuntimeException("error while trying to build jsondeserializer proxy: " + beanClass, e);
        }

    }

    public boolean primitiveType(KType kType, StringBuilder setMethod) {
        Class fieldType = kType.getType();
        if (fieldType == String.class) {
            setMethod.append("jsonParser.getText()");
        } else if (fieldType == Integer.TYPE) {
            setMethod.append("jsonParser.getIntValue()");
        } else if (fieldType == Integer.class) {
            setMethod.append("Integer.valueOf(jsonParser.getIntValue())");
        } else if (fieldType == Float.TYPE) {
            setMethod.append("jsonParser.getFloatValue()");
        } else if (fieldType == Float.class) {
            setMethod.append("Float.valueOf(jsonParser.getFloatValue())");
        } else if (fieldType == Long.class) {
            setMethod.append("Long.valueOf(jsonParser.getLongValue())");
        } else if (fieldType == Long.TYPE) {
            setMethod.append("jsonParser.getLongValue()");
        } else if (fieldType == Double.TYPE) {
            setMethod.append("jsonParser.getDoubleValue()");
        } else if (fieldType == Double.class) {
            setMethod.append("Double.valueOf(jsonParser.getDoubleValue())");
        } else if (fieldType == Short.TYPE) {
            setMethod.append("jsonParser.getShortValue()");
        } else if (fieldType == Short.class) {
            setMethod.append("Short.valueOf(jsonParser.getShortValue())");

        } else if (fieldType == Byte.TYPE) {
            setMethod.append("jsonParser.getByteValue()");
        } else if (fieldType == Byte.class) {
            setMethod.append("Byte.valueOf(jsonParser.getByteValue())");
        } else if (fieldType == BigDecimal.class) {
            setMethod.append("jsonParser.getBigIntegerValue()");
        } else if (fieldType == Boolean.TYPE) {
            setMethod.append("jsonParser.getBooleanValue()");
        } else if (fieldType == Boolean.class) {
            setMethod.append("Boolean.valueOf(jsonParser.getBooleanValue())");
        } else {
            return false;
        }
        return true;
    }

    public boolean pojoType(KType type, StringBuilder functions, StringBuilder parameter) {
        Class rawClass = type.getType();
        if (getDeserialzer(type.getName()) == null) {
            buildJsonDeserializerProxy(type);
        }
        functions.append("KPDeserializer kp=JsonProxyRegister.getDeserialzer(\"");
        functions.append(type.getName());
        functions.append("\");\n");
        parameter.append("(");
        parameter.append(type.getName());
        parameter.append(")kp.from(jsonParser)");
        return true;
    }

    //
//public static class getEntryTypeArray(Class aClass)
//    {
//        ;
////Now assuming that the first parameter to the method is of type List<Integer>
//        ParameterizedType pType = (ParameterizedType) types[0];
//        Class<?> clazz = (Class<?>) pType.getActualTypeArguments()[0];
//    }
    public boolean arrayType(KType aClass, StringBuilder functions, StringBuilder parameter) throws Exception {

        String arrName = "arr" + subFix.getAndIncrement();

        ParameterizedType parameterizedType = null;
        if (aClass.isCollection()) {
            KType elementType = aClass.getTypeBinding().getType()[0];

            if (elementType == null)
                throw new ProxyBuilderException("Unaware  elementType in array or list");


            if (elementType.isPrimitive()) {
                throw new ProxyBuilderException("Unsuport primitive elementType in array or list");
            }
            functions.append(" java.util.List ");
            functions.append(arrName);
            functions.append(" = new java.util.LinkedList();\n");
            functions.append("  while (jsonParser.nextToken() != com.fasterxml.jackson.core.JsonToken.END_ARRAY) {\n");
            StringBuilder parameter2 = new StringBuilder();

            fillParam(elementType, functions, parameter2);

            functions.append(arrName);
            functions.append(".add(");
            functions.append(parameter2);
            functions.append(");\n");
            functions.append("}\n");
            if (aClass.isArray()) {
                functions.append(elementType.getName());
                functions.append("[] ");
                String temArray = arrName + subFix.incrementAndGet();
                functions.append(temArray);
                functions.append("= new ");
                functions.append(elementType.getName());
                functions.append("[");
                functions.append(arrName);
                functions.append(".size()");
                functions.append("];\n");
                functions.append(arrName);
                functions.append(".toArray(");
                functions.append(temArray);
                functions.append(");\n");
                parameter.append(temArray);
            } else {

                parameter.append(arrName);

            }
            return true;
        }
        return false;
    }

    public boolean mapType(KType aClass, StringBuilder functions, StringBuilder parameter) throws Exception {

        String arrName = "map" + subFix.getAndIncrement();

         if (aClass.isMap()) {
            KType valueType = aClass.getTypeBinding().getType()[1];
            if (valueType.isPrimitive()) {
                throw new ProxyBuilderException("Unsuport primitive elementType in map");
            }

            functions.append(" java.util.Map ");
            functions.append(arrName);
            functions.append(" = new ");
            if (aClass.getType() == Map.class) {
                functions.append("java.util.HashMap");
            } else if (aClass.isAbstract()) {
                throw new ProxyBuilderException("Unsuport Abstract map");

            } else {
                functions.append(aClass.getName());

            }

            functions.append("(); \n ");

            functions.append(" while (jsonParser.nextToken() != com.fasterxml.jackson.core.JsonToken.END_OBJECT) { \n");
            functions.append(" String name = jsonParser.getCurrentName();\n");
            StringBuilder parameter2 = new StringBuilder();
            fillParam(valueType, functions, parameter2);
            functions.append(arrName);
            functions.append(".put(");
            functions.append("name");
            functions.append(",");
            functions.append(parameter2);
            functions.append(");\n");
            functions.append("}\n");
            parameter.append(arrName);
            return true;
        }
        return false;
    }

    public void fillParam(KType valueType, StringBuilder functions, StringBuilder parameter) throws Exception {
        if (primitiveType(valueType, parameter)) {

        } else if (arrayType(valueType, functions, parameter)) {


        } else if (mapType(valueType, functions, parameter)) {

        } else {
            pojoType(valueType, functions, parameter);
        }
    }


    public synchronized void register(String pakages) throws ProxyBuilderException, IllegalAccessException, InstantiationException {
        Reflections reflections = new Reflections(pakages);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(KPJsonProxy.class);
        for (Class<?> bd : annotated) {
//           if(!hasConstructorNoParam(bd.getClass()))
//           {
//               throw new ProxyBuilderException(bd.getName()+" must be constructor no parameter");
//           }
            buildJsonDeserializerProxy(new KType(bd));


        }


    }


}
