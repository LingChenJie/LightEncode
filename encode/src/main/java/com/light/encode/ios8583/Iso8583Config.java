package com.light.encode.ios8583;

import android.util.Xml;

import com.light.encode.util.CloneUtil;
import com.light.encode.util.CollectionUtil;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Iso8583Config {

    private static HashMap<String, Field> originFieldMap = new HashMap<>();

    public static void setBitmapConfig(InputStream input) {
        List<Field> fields = parseBitmap(input);
        if (fields == null) {
            throw new RuntimeException("parse bitmap fail.");
        }
        setBitmapConfig(fields);
    }

    public static void setBitmapConfig(List<Field> list) {
        if (list != null && list.size() > 0) {
            HashMap<String, Field> map = new HashMap<>();
            for (int i = 0; i < list.size(); i++) {
                Field field = list.get(i);
                int position = field.getPosition();
                if (position >= Constant.Position.MIN && position <= Constant.Position.MAX) {
                    String name = Helper.getFieldName(position);
                    map.put(name, field);
                } else {
                    throw new RuntimeException("Field [" + i + "] out of configuration range.");
                }
            }
            originFieldMap = map;
        }
    }

    public static Map<String, Field> getFieldMapConfig() {
        if (CollectionUtil.isEmpty(originFieldMap)) {
            throw new RuntimeException("You must call Iso8583Config.setBitmapConfig method first.");
        }
        try {
            return CloneUtil.deepClone(originFieldMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    public static Map<String, Field> getOriginFieldMap() {
        if (CollectionUtil.isEmpty(originFieldMap)) {
            throw new RuntimeException("You must call Iso8583Config.setBitmapConfig method first.");
        }
        return originFieldMap;
    }

    private static List<Field> parseBitmap(InputStream input) {
        try {
            List<Field> fields = new ArrayList<>();
            Field field;
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(input, "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tagName = parser.getName();
                    if (tagName.equals(Constant.Field.FIELD)) {
                        String position = parser.getAttributeValue(null, Constant.Field.POSITION);
                        String lengthType = parser.getAttributeValue(null, Constant.Field.LENGTH_TYPE);
                        String dataLength = parser.getAttributeValue(null, Constant.Field.DATA_LENGTH);
                        String dataEncode = parser.getAttributeValue(null, Constant.Field.DATA_ENCODE);
                        String alignType = parser.getAttributeValue(null, Constant.Field.ALIGN_TYPE);
                        String padding = parser.getAttributeValue(null, Constant.Field.PADDING);
                        field = new Field.Builder()
                                .padding(position)
                                .lengthType(lengthType)
                                .dataLength(dataLength)
                                .dataEncode(dataEncode)
                                .alignType(alignType)
                                .padding(padding)
                                .build();
                        fields.add(field);
                    }
                }
                eventType = parser.next();
            }
            input.close();
            return fields;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}