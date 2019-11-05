package com.kp.taskmanager.thread;

import com.kp.common.data.DataType;
import com.kp.common.data.vo.VObject;

public abstract class ParameterLoader {
    final public static String NAME = "name";
    final public static String TYPE = "type";
    final public static String REQUIREMENT = "requirement";
    final public static String DEFINATION = "defination";


    /**
     * @deprecated
     */
    public static VObject createParameterDefinition(String strName, DataType dataType, Object requirement) {
        if (dataType == DataType.OBJECT || dataType == DataType.ARRAY) {
            throw new IllegalArgumentException("Method don't support Object type please using createParameterDefinition(String strName,VObject objDefination,Object requirement) instead");
        }
        VObject vObject = new VObject();
        vObject.put(NAME, strName);
        vObject.put(TYPE, dataType.name());

        if (requirement != null) {
            vObject.put(REQUIREMENT, requirement);
        }
        return vObject;


    }

    public static VObject createParameterDefinition(String strName, VObject objDefination, Object requirement) {
        VObject vObject = new VObject();
        vObject.put(NAME, strName);
        vObject.put(TYPE, DataType.OBJECT);
        vObject.put(DEFINATION, objDefination);
        if (objDefination == null) {
            throw new IllegalArgumentException(" objDefination must be not null");

        }
        if (requirement != null) {
            vObject.put(REQUIREMENT, requirement);
        }
        return vObject;


    }

    public abstract void fillParameter(VObject vObject) throws Exception;

    public abstract void validateParameter() throws Exception;

    public abstract VObject getParameterDefinition();


}
