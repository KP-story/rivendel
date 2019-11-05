package com.kp.pojo;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class ClassInfo {
    private List<AttributeInfo> attributes;
    private String simpleName;
    private String name;
    private String pk;
    private KType type;

    public KType getType() {
        return type;
    }

    public void setType(Class fieldType, Type type) {
        if (type != null && type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Class rawType = (Class) parameterizedType.getRawType();
            this.type = new KType(rawType, parameterizedType.getActualTypeArguments());
            return;

        }
        this.type = new KType(fieldType);
    }

    public List<AttributeInfo> getAttributes() {
        return attributes;

    }

    public void setAttributes(List<AttributeInfo> attributes) {
        this.attributes = attributes;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public static class AttributeInfo {
        private String attrName;
        private ClassInfo classInfo;

        public String getAttrName() {
            return attrName;
        }

        public void setAttrName(String attrName) {
            this.attrName = attrName;
        }

        public ClassInfo getClassInfo() {
            return classInfo;
        }

        public void setClassInfo(ClassInfo classInfo) {
            this.classInfo = classInfo;
        }

    }
}
