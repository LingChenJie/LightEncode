package com.light.encode.ios8583;

import com.light.encode.util.ByteUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class Iso8583 implements Serializable {

    private static final long serialVersionUID = -6514454101496082018L;

    private int length;// 报文长度
    private byte[] header;// 报文头
    private String bitmap;// 位图
    private String msgType;// 消息类型
    private byte[] allFieldData;// 域数据
    private Map<String, Field> fieldMap;// 域集合

    private byte[] dataBytes;// 数据
    private int headerLength;// 报文头长度
    private boolean hasBitmap;// 是否有位图
    private int lengthLength;// 报文长度的长度
    private List<Field> fieldConfigList;

    private Iso8583(int length, byte[] header, String msgType, String bitmap, byte[] allFieldData, Map<String, Field> fieldMap) {
        this.length = length;
        this.header = header;
        this.msgType = msgType;
        this.bitmap = bitmap;
        this.allFieldData = allFieldData;
        this.fieldMap = fieldMap;
    }

    private Iso8583(int lengthLength, byte[] header, String msgType, boolean hasBitmap, Map<String, Field> fieldMap) {
        this.lengthLength = lengthLength;
        this.header = header;
        this.msgType = msgType;
        this.hasBitmap = hasBitmap;
        this.fieldMap = fieldMap;
    }

    private Iso8583(byte[] dataBytes, int lengthLength, int headerLength, List<Field> fieldConfigList) {
        this.dataBytes = dataBytes;
        this.lengthLength = lengthLength;
        this.headerLength = headerLength;
        this.fieldConfigList = fieldConfigList;
    }


    public byte[] encode() {
        if (fieldMap == null || fieldMap.isEmpty()) {
            throw new RuntimeException("Data error, fieldMap cannot be null");
        }
        return Iso8583Encode.encode(fieldMap, lengthLength, header, msgType, hasBitmap);
    }

    public Iso8583 decode() {
        Map<String, Field> fieldConfigMap = Helper.getFieldMapConfig();
        if (fieldConfigList != null && !fieldConfigList.isEmpty()) {
            for (int i = 0; i < fieldConfigList.size(); i++) {
                Field field = fieldConfigList.get(i);
                int position = field.getPosition();
                if (position >= 0 && position <= 128) {
                    String name = Helper.getFieldName(position);
                    fieldConfigMap.put(name, field);
                } else {
                    throw new RuntimeException("Field [" + i + "] out of configuration range.");
                }
            }
        }
        return decode(fieldConfigMap);
    }

    public Iso8583 decode(Map<String, Field> fieldConfigMap) {
        if (dataBytes == null || dataBytes.length == 0) {
            throw new RuntimeException("Data error, dataBytes cannot be null");
        }
        return Iso8583Decode.decode(dataBytes, lengthLength, headerLength, fieldConfigMap);
    }

    public static final class Builder {

        private int length;
        private byte[] header;
        private String bitmap;
        private String msgType;
        private byte[] allFieldData;
        private final Map<String, Field> fieldMap = new HashMap<>();

        public Builder addLength(int length) {
            this.length = length;
            return this;
        }

        public Builder addHeader(byte[] bytes) {
            this.header = bytes;
            return this;
        }

        public Builder addBitmap(String bitmap) {
            this.bitmap = bitmap;
            return this;
        }

        public Builder addMsgType(String bytes) {
            this.msgType = bytes;
            return this;
        }

        public Builder addAllFieldData(byte[] bytes) {
            this.allFieldData = bytes;
            return this;
        }

        public Builder addMessageField(Map<String, Field> map) {
            fieldMap.putAll(map);
            return this;
        }

        public Iso8583 build() {
            return new Iso8583(length, header, msgType, bitmap, allFieldData, fieldMap);
        }

    }

    public static final class EncodeBuilder {

        private byte[] header;
        private String msgType;
        private boolean hasBitmap = true;
        private int lengthLength = 2;
        private final Map<String, Field> fieldMap = new HashMap<>();

        public EncodeBuilder addHeader(byte[] bytes) {
            this.header = bytes;
            return this;
        }

        public EncodeBuilder addHeader(String hexString) {
            this.header = ByteUtil.hexString2Bytes(hexString);
            return this;
        }

        public EncodeBuilder addMsgType(String value) {
            addField(0, value);
            return this;
        }

        public EncodeBuilder addField(Map<String, Field> map) {
            fieldMap.putAll(map);
            return this;
        }

        public EncodeBuilder addField(int position, String value) {
            if (position == 0) {
                this.msgType = value;
            }
            String name = Helper.getFieldName(position);
            Field field = Helper.getFieldClone(name);
            if (field != null) {
                field.setDataString(value);
                fieldMap.put(name, field);
            } else {
                throw new RuntimeException("bitmap does not configure the field for position " + position);
            }
            return this;
        }

        public EncodeBuilder addField(Field field) {
            int position = field.getPosition();
            String name = Helper.getFieldName(position);
            fieldMap.put(name, field);
            return this;
        }

        public EncodeBuilder hasBitmap(boolean hasBitmap) {
            this.hasBitmap = hasBitmap;
            return this;
        }

        public EncodeBuilder addLengthLength(int lengthLength) {
            this.lengthLength = lengthLength;
            return this;
        }

        public Iso8583 build() {
            return new Iso8583(lengthLength, header, msgType, hasBitmap, fieldMap);
        }

    }

    public static final class DecodeBuilder {

        private byte[] dataBytes;
        private int headerLength = 0;
        private int lengthLength = 2;
        private List<Field> fieldListConfig;

        public DecodeBuilder addDataBytes(byte[] dataBytes) {
            this.dataBytes = dataBytes;
            return this;
        }

        public DecodeBuilder addHeaderLength(int headerLength) {
            this.headerLength = headerLength;
            return this;
        }

        public DecodeBuilder addLengthLength(int lengthLength) {
            this.lengthLength = lengthLength;
            return this;
        }

        public DecodeBuilder addFieldListConfig(List<Field> fieldList) {
            if (fieldListConfig == null) {
                fieldListConfig = new ArrayList<>();
            }
            this.fieldListConfig.addAll(fieldList);
            return this;
        }

        public DecodeBuilder addFieldConfig(Field field) {
            if (fieldListConfig == null) {
                fieldListConfig = new ArrayList<>();
            }
            fieldListConfig.add(field);
            return this;
        }

        public Iso8583 build() {
            return new Iso8583(dataBytes, lengthLength, headerLength, fieldListConfig);
        }

    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getHeader() {
        return header;
    }

    public void setHeader(byte[] header) {
        this.header = header;
    }

    public String getBitmap() {
        return bitmap;
    }

    public void setBitmap(String bitmap) {
        this.bitmap = bitmap;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public byte[] getAllFieldData() {
        return allFieldData;
    }

    public void setAllFieldData(byte[] allFieldData) {
        this.allFieldData = allFieldData;
    }

    public Map<String, Field> getFieldMap() {
        return new TreeMap<>(fieldMap);
    }

    public void setFieldMap(Map<String, Field> fieldMap) {
        this.fieldMap = fieldMap;
    }

}