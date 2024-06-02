package com.light.encode.ios8583;

import com.android.architecture.utils.LogUtils;
import com.light.encode.util.ByteUtil;
import com.light.encode.util.L;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class Helper {

    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_RIGHT = 1;

    public static final int LENGTH_VAR_NONE = 0;
    public static final int LENGTH_VAR_PAIR = 2;
    public static final int LENGTH_VAR_TRIP = 3;

    public static final int ENCODE_BCD = 0;
    public static final int ENCODE_BIT = 1;
    public static final int ENCODE_ASC = 2;

    public static String getFieldName(int position) {
        return "F" + String.format(Locale.getDefault(), "%03d", position);
    }

    public static Field getField(int position) {
        return new Field.Builder().position(position).build();
    }

    public static Map<String, Field> getFieldMapConfig() {
        return Iso8583Config.getFieldMapConfig();
    }

    public static Field getFieldClone(String fieldName) {
        Field field = Iso8583Config.getOriginFieldMap().get(fieldName);
        if (field != null) {
            // 克隆一份副本
            Field clone = field.cloneField();
            return clone;
        }
        return null;
    }

    public static Map<String, Field> assembly(String bitmapHexString) {
        Map<String, Field> fieldMap = Iso8583Config.getFieldMapConfig();
        return assembly(bitmapHexString, fieldMap);
    }

    public static Map<String, Field> assembly(String bitmapHexString, Map<String, Field> fieldMap) {
        HashMap<String, Field> map = new HashMap<>();
        byte[] bitmapBytes = ByteUtil.hexString2Bytes(bitmapHexString);
        boolean[] bitmapBinaryBytes = ByteUtil.bytes2BinaryBytes(bitmapBytes);
        for (int position = 0; position < bitmapBinaryBytes.length; position++) {
            boolean bool = bitmapBinaryBytes[position];
            if (bool) {
                String name = Helper.getFieldName(position);
                Field field = fieldMap.get(name);
                if (field != null) {
                    map.put(name, field);
                } else {
                    LogUtils.w(L.TAG,"Field [" + position + "] not configured.");
                }
            }
        }
        return map;
    }

}