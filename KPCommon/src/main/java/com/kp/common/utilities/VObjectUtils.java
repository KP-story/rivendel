package com.kp.common.utilities;

import com.kp.common.data.vo.VArray;
import com.kp.common.data.vo.VObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class VObjectUtils {

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static final VArray toVArray(Object obj) {
        final VArray list = new VArray();
        if (ArrayUtils.isArrayOrCollection(obj.getClass())) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS zzz");
            ArrayUtils.foreach(obj, new ArrayUtils.ForeachCallback<Object>() {

                @Override
                public void apply(Object element) {
                    if (element == null) {
                        list.add(null);
                    } else if (DataTypeUtils.isPrimitiveOrWrapperType(element.getClass())) {
                        list.add(element);
                    } else if (ArrayUtils.isArrayOrCollection(element.getClass())) {
                        list.add(toVArray(element));
                    } else if (element instanceof Date) {
                        list.add(df.format(element));
                    } else if (element.getClass().isEnum()) {
                        list.add(element.toString());
                    } else if (element instanceof Throwable) {

                    } else {
                        list.add(toVObjectRecursive(element));
                    }
                }
            });
        }
        return list;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final VObject toVObjectRecursive(Object obj) {
        if (obj == null) {
            return null;
        }
        if (DataTypeUtils.isPrimitiveOrWrapperType(obj.getClass())) {
            throw new RuntimeException("cannot convert primitive type : " + obj.getClass() + " to Map");
        }
        if (ArrayUtils.isArrayOrCollection(obj.getClass())) {
            throw new RuntimeException("cannot convert array|collection : " + obj.getClass() + " to Map");
        }
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS zzz");
        Map<?, Object> rawMap = (Map) obj;
        VObject map = new VObject();
        for (Map.Entry<?, Object> child : rawMap.entrySet()) {
            String field = String.valueOf(child.getKey());
            Object value = child.getValue();
            if (value == null) {
                map.put(field, null);
            } else if (DataTypeUtils.isPrimitiveOrWrapperType(value.getClass())) {
                map.put(field, value);
            } else if (ArrayUtils.isArrayOrCollection(value.getClass())) {
                map.put(field, toVArray(value));
            } else if (value instanceof Date) {
                map.put(field, df.format(value));
            } else if (value.getClass().isEnum()) {
                map.put(field, value.toString());
            } else if (value instanceof Throwable) {
            } else {
                map.put(field, toVObjectRecursive(value));
            }
        }
        return map;
    }
}
