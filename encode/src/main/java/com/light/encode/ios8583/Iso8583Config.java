package com.light.encode.ios8583;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.light.encode.ios8583.hide.Iso8583Helper;
import com.light.encode.util.CloneUtil;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Iso8583Config {

    public static final String TAG = "Iso8583";

    private static HashMap<String, Field> allFieldMap = new HashMap<>();

//    static {
//        setBitmapFieldConfig(Iso8583Helper.FIELD_CONFIG_128());
//    }

    public static void setBitmapFieldConfig(File file) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            int length = inputStream.available();
            byte[] buffer = new byte[length];
            inputStream.read(buffer);
            inputStream.close();
            String jsonString = new String(buffer, StandardCharsets.UTF_8);
            setBitmapFieldConfig(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setBitmapFieldConfig(String jsonString) {
        try {
            Type type = new TypeToken<List<Field>>() {
            }.getType();
            List<Field> list = new Gson().fromJson(jsonString, type);
            setBitmapFieldConfig(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setBitmapFieldConfig(List<Field> list) {
        if (list != null && list.size() > 0) {
            HashMap<String, Field> map = new HashMap<>();
            for (int i = 0; i < list.size(); i++) {
                Field field = list.get(i);
                int position = field.getPosition();
                if (position >= 0 && position <= 128) {
                    String name = Iso8583Helper.getMessageFieldName(position);
                    map.put(name, field);
                } else {
                    Logger.getLogger(Iso8583Config.TAG).log(Level.WARNING, "Field [" + i + "] out of configuration range.");
                }
            }
            allFieldMap = map;
        }
    }

    public static Map<String, Field> getBitmapFieldConfig() {
        if (allFieldMap.size() == 0) {
            throw new RuntimeException("You must call Iso8583Config.setBitmapXxxConfig method first.");
        }
        try {
            return CloneUtil.deepClone(allFieldMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    public static Map<String, Field> getOriginBitmapFieldConfig() {
        if (allFieldMap.size() == 0) {
            throw new RuntimeException("You must call Iso8583Config.setBitmapXxxConfig method first.");
        }
        return allFieldMap;
    }

}