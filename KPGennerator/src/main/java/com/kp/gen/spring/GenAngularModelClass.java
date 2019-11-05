package com.kp.gen.spring;

import com.kp.common.utilities.StringUtils;
import com.kp.pojo.ClassInfo;
import com.kp.pojo.ClassUtils;
import com.kp.pojo.KType;
import com.kp.pojo.ProxyBuilderException;
import org.reflections.Reflections;

import javax.persistence.Entity;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class GenAngularModelClass {

    public List<ClassInfo> loadClass(String pakage) {
        List<ClassInfo> classInfos = new LinkedList<>();
        Reflections reflections = new Reflections(pakage);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Entity.class);
        List<GenSpringClass.MyEntity> myEntities = new LinkedList<>();
        for (Class<?> bd : annotated) {
            ClassInfo classInfo = ClassUtils.extractInfo(bd, null,true);
            classInfos.add(classInfo);
        }
        return classInfos;

    }

    public void genClass(String pakage, String modelPath) throws Exception {
        List<ClassInfo> entities = loadClass(pakage);
        for (ClassInfo entity : entities) {
            buildModel(entity, modelPath);
        }

    }

    public void buildModel(ClassInfo entity, String path) throws Exception {
        StringBuilder classBody = new StringBuilder();
        StringBuilder importBody = new StringBuilder();
        classBody.append("export class ");
        classBody.append(entity.getSimpleName()).append("{\n");
        for (ClassInfo.AttributeInfo attributeInfo : entity.getAttributes()) {
            StringBuilder dataType = new StringBuilder();
            fillParam(attributeInfo.getClassInfo().getType(), importBody, dataType);
            classBody.append("public ");
            classBody.append(attributeInfo.getAttrName());
            classBody.append(" : ");
            classBody.append(dataType);
            classBody.append(";\n");

        }

        classBody.append("\n}");
        classBody = importBody.append(classBody);


        File file = new File(path + "/" + StringUtils.cammelToSnake(entity.getSimpleName()) + ".model.ts");
        if (!file.exists()) {

            if (!file.createNewFile()) {
                System.out.println("File already exists.");
            }
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(classBody.toString());
            fileWriter.flush();
            fileWriter.close();
        } else {
            System.out.println(file.getName() + "  already exists.");

        }
    }

    public boolean primitiveType(KType kType, StringBuilder dataType) {
        Class fieldType = kType.getType();
        if(fieldType== Date.class||fieldType== java.sql.Date.class)
        {
            dataType.append("Date");

        }else
        if(fieldType==BigInteger.class)
        {
            dataType.append("number");

        }else
        if (fieldType == Enum.class) {
            dataType.append("enum");

        } else if (fieldType == String.class) {
            dataType.append("string");
        } else if (fieldType == Integer.TYPE) {
            dataType.append("number");
        } else if (fieldType == Integer.class) {
            dataType.append("number");
        } else if (fieldType == Float.TYPE) {
            dataType.append("number");
        } else if (fieldType == Float.class) {
            dataType.append("number");
        } else if (fieldType == Long.class) {
            dataType.append("number");
        } else if (fieldType == Long.TYPE) {
            dataType.append("number");
        } else if (fieldType == Double.TYPE) {
            dataType.append("number");
        } else if (fieldType == Double.class) {
            dataType.append("number");
        } else if (fieldType == Short.TYPE) {
            dataType.append("number");
        } else if (fieldType == Short.class) {
            dataType.append("number");

        } else if (fieldType == Byte.TYPE) {
            dataType.append("number");
        } else if (fieldType == Byte.class) {
            dataType.append("number");
        } else if (fieldType == BigDecimal.class) {
            dataType.append("number");
        } else if (fieldType == Boolean.TYPE) {
            dataType.append("boolean");
        } else if (fieldType == Boolean.class) {
            dataType.append("boolean");
        } else {
            return false;
        }
        return true;
    }

    public boolean pojoType(KType kType, StringBuilder importBody, StringBuilder dataType) {
        importBody.append("import {");
        importBody.append(kType.getSimpleName());
        importBody.append("} from \'");
        importBody.append("./");
        importBody.append(StringUtils.cammelToSnake(kType.getSimpleName()));
        importBody.append(".model");
        importBody.append("\';\n");
        dataType.append(kType.getSimpleName());


        return true;
    }


    public boolean arrayType(KType kType, StringBuilder importBody, StringBuilder dataType) throws Exception {


        ParameterizedType parameterizedType = null;
        if (kType.isCollection()) {
            KType elementType = kType.getTypeBinding().getType()[0];

            if (elementType == null)
                throw new ProxyBuilderException("Unaware  elementType in array or list");
            StringBuilder parameter2 = new StringBuilder();

            fillParam(elementType, importBody, parameter2);
            dataType.append(parameter2);
            dataType.append("[]");

            return true;
        }
        return false;
    }


    public void fillParam(KType kType, StringBuilder importBody, StringBuilder dataType) throws Exception {
        if (primitiveType(kType, dataType)) {

        } else if (mapType(kType, importBody, dataType)) {

        } else if (arrayType(kType, importBody, dataType)) {

        } else if (pojoType(kType, importBody, dataType)) {

        }
    }


    public boolean mapType(KType kType, StringBuilder importBody, StringBuilder dataType) throws Exception {


        ParameterizedType parameterizedType = null;
        if (kType.isMap()) {
            KType valueType = kType.getTypeBinding().getType()[1];
            if (valueType.isPrimitive()) {
                throw new ProxyBuilderException("Unsuport primitive elementType in map");
            }

            if (primitiveType(kType, dataType)) {

            } else if (mapType(kType, importBody, dataType)) {

            } else if (arrayType(kType, importBody, dataType)) {

            } else if (pojoType(kType, importBody, dataType)) {

            }
            dataType.append("Map");
            return true;
        }
        return false;
    }

}
