package com.light.encode.ios8583.hide;

import com.light.encode.ios8583.Iso8583Config;
import com.light.encode.ios8583.Field;
import com.light.encode.util.ByteUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class BitmapHelper {

    public static Map<String, Field> getDefaultFieldConfig() {
        return Iso8583Config.getBitmapFieldConfig();
    }

    public static Field getMessageFieldClone(String name) {
        Field field = Iso8583Config.getOriginBitmapFieldConfig().get(name);
        if (field != null) {
            // 克隆一份副本
            Field clone = field.cloneField();
            return clone;
        }
        return null;
    }

    public static Map<String, Field> assembly(String bitmapHexString) {
        Map<String, Field> fieldMap = Iso8583Config.getBitmapFieldConfig();
        return assembly(bitmapHexString, fieldMap);
    }

    public static Map<String, Field> assembly(String bitmapHexString, Map<String, Field> fieldMap) {
        HashMap<String, Field> map = new HashMap<>();
        byte[] bitmapBytes = ByteUtil.hexString2Bytes(bitmapHexString);
        boolean[] bitmapBinaryBytes = ByteUtil.bytes2BinaryBytes(bitmapBytes);
        for (int i = 0; i < bitmapBinaryBytes.length; i++) {
            boolean bool = bitmapBinaryBytes[i];
            if (bool) {
                String name = Iso8583Helper.getMessageFieldName(i);
                Field field = fieldMap.get(name);
                if (field != null) {
                    map.put(name, field);
                } else {
                    Logger.getLogger(Iso8583Config.TAG).log(Level.WARNING, "Field [" + i + "] not configured.");
                }
            }
        }
        return map;
    }

    public static String getBitmap128String() {
        StringBuilder bitmap128String = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            bitmap128String.append("F");
        }
        return bitmap128String.toString();
    }

}