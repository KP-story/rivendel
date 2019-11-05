package com.kp.common.data.vo;


import java.util.ArrayList;
import java.util.Collection;

import static com.kp.common.utilities.DataTypeUtils.*;
import static com.kp.common.utilities.VObjectUtils.toVArray;

public class VArray extends ArrayList {

    public VArray(int initialCapacity) {
        super(initialCapacity);
    }

    public VArray() {
        super();
    }

    public VArray(Collection c) {
        super(c);
    }

    //
    public static VArray fromObject(Object data) {
        if (data == null) {
            return null;
        } else if (isArrayOrCollection(data.getClass())) {
            final VArray result = toVArray(data);
            return result;
        }
        throw new IllegalArgumentException("cannot convert " + data.getClass() + " to PuArray");
    }


    public boolean getBoolean(int i) {
        return getBooleanValueFrom(
                this.get(i) instanceof byte[] ? new String((byte[]) this.get(i)) : this.get(i));
    }

    public byte getByte(int i) {
        return getByteValueFrom(get(i));
    }

    public short getShort(int i) {
        return getShortValueFrom(this.get(i));
    }

    public int getInteger(int i) {
        return getIntegerValueFrom(get(i));
    }

    public float getFloat(int i) {
        return getFloatValueFrom(this.get(i));
    }

    public long getLong(int i) {
        return getLongValueFrom(this.get(i));
    }

    public double getDouble(int i) {
        return getDoubleValueFrom(this.get(i));
    }

    public String getString(int i) {

        return getStringValueFrom(this.get(i));
    }

    public String getString(int i, String defaultValue) {
        String value = this.getString(i);
        if (value != null) {
            return this.getString(i);
        }
        return defaultValue;
    }

    public byte[] getRaw(int i) {
        Object data = this.get(i);
        if (data instanceof String) {
            return ((String) data).getBytes();
        }
        return (byte[]) data;
    }

    public VObject getVObject(int i) {
        return (VObject) get(i);
    }

    public byte[] getRaw(int i, byte[] defaultValue) {
        byte[] value = getRaw(i);
        if (value != null) {
            return this.getRaw(i);

        }
        return defaultValue;

    }

    void append(int numTab, StringBuilder sb) {
        sb.append("[");
        boolean flag = false;
        for (Object value : this) {
            if (flag) {
                sb.append(", ");
            } else {
                flag = true;
            }
            if (value instanceof VObject) {
                VObject puo = (VObject) value;
                puo.append(numTab + 1, sb);

            } else {
                sb.append(value);
            }
        }
        sb.append("]");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        append(0, sb);
        return sb.toString();
    }


}
