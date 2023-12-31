package com.light.encode.ios8583;

import com.light.encode.util.ByteUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Iso8583 implements Serializable {

    private static final long serialVersionUID = -6514454101496082018L;

    private int length;// 报文长度
    private byte[] header;// 报文头
    private String bitmap;// 位图
    private byte[] msgType;// 消息类型
    private byte[] allFieldData;// 域数据
    private Map<String, Field> fieldMap;// 域集合

    private byte[] dataBytes;// 数据
    private int headerLength;// 报文头长度
    private int msgTypeLength;// 消息类型长度
    private boolean hasBitmap;// 是否有位图
    private int lengthLength;// 报文长度的长度
    private List<Field> fieldConfigList;

    private Iso8583(int length, byte[] header, byte[] msgType, String bitmap, byte[] allFieldData, Map<String, Field> fieldMap) {
        this.length = length;
        this.header = header;
        this.bitmap = bitmap;
        this.msgType = msgType;
        this.allFieldData = allFieldData;
        this.fieldMap = fieldMap;
    }

    private Iso8583(int lengthLength, byte[] header, byte[] msgType, boolean hasBitmap, Map<String, Field> fieldMap) {
        this.header = header;
        this.msgType = msgType;
        this.fieldMap = fieldMap;
        this.hasBitmap = hasBitmap;
        this.lengthLength = lengthLength;
    }

    private Iso8583(byte[] dataBytes, int lengthLength, int headerLength, int msgTypeLength, List<Field> fieldConfigList) {
        this.dataBytes = dataBytes;
        this.headerLength = headerLength;
        this.msgTypeLength = msgTypeLength;
        this.fieldConfigList = fieldConfigList;
        this.lengthLength = lengthLength;
    }


    public byte[] encode() throws Exception {
        if (fieldMap == null || fieldMap.size() == 0) {
            throw new Exception("Data error, fieldMap cannot be null");
        }
        return Iso8583Encode.encode(fieldMap, lengthLength, header, msgType, hasBitmap);
    }

    public Iso8583 decode() {
        Map<String, Field> fieldConfigMap = Helper.getFieldMapConfig();
        if (fieldConfigList != null && fieldConfigList.size() > 0) {
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
        return Iso8583Decode.decode(dataBytes, lengthLength, headerLength, msgTypeLength, fieldConfigMap);
    }

    public static final class Builder {

        private int length;
        private byte[] header;
        private String bitmap;
        private byte[] msgType;
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

        public Builder addMsgType(byte[] bytes) {
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
        private byte[] msgType;
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

        public EncodeBuilder addMsgType(byte[] bytes) {
            this.msgType = bytes;
            return this;
        }

        public EncodeBuilder addMsgType(String hexString) {
            this.msgType = ByteUtil.hexString2Bytes(hexString);
            return this;
        }

        public EncodeBuilder addField(Map<String, Field> map) {
            fieldMap.putAll(map);
            return this;
        }

        public EncodeBuilder addField(int position, String value) {
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
        private int messageLength = 2;
        private int lengthLength = 2;
        private List<Field> fieldConfigList;

        public DecodeBuilder addDataBytes(byte[] dataBytes) {
            this.dataBytes = dataBytes;
            return this;
        }

        public DecodeBuilder addHeaderLength(int headerLength) {
            this.headerLength = headerLength;
            return this;
        }

        public DecodeBuilder addMessageLength(int messageLength) {
            this.messageLength = messageLength;
            return this;
        }

        public DecodeBuilder addLengthLength(int lengthLength) {
            this.lengthLength = lengthLength;
            return this;
        }

        public DecodeBuilder addFieldConfigList(List<Field> fieldConfigList) {
            this.fieldConfigList = fieldConfigList;
            return this;
        }

        public Iso8583 build() {
            return new Iso8583(dataBytes, lengthLength, headerLength, messageLength, fieldConfigList);
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

    public byte[] getMsgType() {
        return msgType;
    }

    public void setMsgType(byte[] msgType) {
        this.msgType = msgType;
    }

    public byte[] getAllFieldData() {
        return allFieldData;
    }

    public void setAllFieldData(byte[] allFieldData) {
        this.allFieldData = allFieldData;
    }

    public Map<String, Field> getFieldMap() {
        return fieldMap;
    }

    public void setFieldMap(Map<String, Field> fieldMap) {
        this.fieldMap = fieldMap;
    }

}