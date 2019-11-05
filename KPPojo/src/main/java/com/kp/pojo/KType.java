package com.kp.pojo;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class KType implements Type {
    private String name;
    private Class type;
    private TypeBinding typeBinding;

    public KType(Class type) {
        this.type = type;
        this.name = type.getName();
        if (isArray()) {
            TypeBinding typeBinding = new TypeBinding(new KType(this.type.getComponentType()));
            setTypeBinding(typeBinding);
        } else {
            setTypeBinding(EMPTY());
        }
    }

    ;

    public KType(Class type, Type... genericTypes) {
        this.type = type;
        this.name = type.getName();
        if (isArray()) {
            TypeBinding typeBinding = new TypeBinding(new KType(this.type.getComponentType()));
            setTypeBinding(typeBinding);
        } else if (genericTypes != null && genericTypes.length > 0) {
            List<KType> gtypes = new LinkedList<>();
            for (Type gType : genericTypes) {
                if (gType instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) gType;
                    Class<?> rawType = (Class<?>) parameterizedType.getRawType();
                    KType kType = new KType(rawType, parameterizedType.getActualTypeArguments());
                    gtypes.add(kType);
                    System.out.println("parameter" + rawType.getName());

                } else if (gType instanceof Class) {
                    Class<?> rawType = (Class<?>) gType;
                    KType kType = new KType(rawType);
                    gtypes.add(kType);
                    System.out.println("primitive" + this.name);

                }
            }
            KType[] kType = new KType[gtypes.size()];
            TypeBinding typeBinding = new TypeBinding(gtypes.toArray(kType));
            setTypeBinding(typeBinding);

        } else

        {
            setTypeBinding(EMPTY());
        }

    }

    protected static TypeBinding EMPTY() {
        return new TypeBinding(new KType[0]);
    }

    public String getJsonDeClassName() {
        return type.getName().replace(".", "_") + "JsonDeserialize";

    }

    public String getSimpleName() {
        return type.getSimpleName();
    }

    public TypeBinding getTypeBinding() {
        return typeBinding;
    }

    public void setTypeBinding(TypeBinding typeBinding) {
        this.typeBinding = typeBinding;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public boolean isInterface() {
        return type.isInterface();
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(type.getModifiers());
    }

    public boolean isPrimitive() {
        return type.isPrimitive();

    }

    public boolean isCollection() {
        return type.isArray() || List.class.isAssignableFrom(type);

    }

    public boolean isArray() {
        return type.isArray();

    }

    public boolean isMap() {
        return Map.class.isAssignableFrom(type);

    }

    public List<SetterMethodSignature> extractSetter() {
        return ClassUtils.extractSetterMethodSignatures(this.type);
    }

    public Object initInstance() throws IllegalAccessException, InstantiationException {
        return this.type.newInstance();
    }

    public static class TypeBinding {
        KType[] type;

//        public TypeBinding(Type[] genericTypes) {
//            if(genericTypes!=null&&genericTypes.length>0)
//            {
//                List<KType> gtypes= new LinkedList<>();
//                for(Type gType:genericTypes)
//                {
//                    if (gType instanceof ParameterizedType)
//                    {
//                        ParameterizedType parameterizedType= (ParameterizedType) gType;
//                        Class<?> rawType = (Class<?>) parameterizedType.getRawType();
//                        KType kType=new KType(rawType,parameterizedType.getActualTypeArguments());
//                        gtypes.add(kType);
//
//                    }
//                }
//                type= new KType[gtypes.size()];
//                 gtypes.toArray(type);
//
//            }
//
//        }

        public TypeBinding(KType... genericType) {
            this.type = genericType;
        }

        public KType[] getType() {
            return type;
        }

        public void setType(KType[] type) {
            this.type = type;
        }
    }
}
