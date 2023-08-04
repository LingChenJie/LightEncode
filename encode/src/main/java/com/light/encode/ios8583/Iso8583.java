package com.light.encode.ios8583;


import com.light.encode.ios8583.hide.BitmapHelper;
import com.light.encode.ios8583.hide.Iso8583Decode;
import com.light.encode.ios8583.hide.Iso8583Encode;
import com.light.encode.ios8583.hide.Iso8583Helper;
import com.light.encode.util.ByteUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Iso8583 implements Serializable {

    private static final long serialVersionUID = -6514454101496082018L;

    private int length;
    private byte[] header;
    private String bitmap;
    private byte[] msgType;
    private byte[] allFieldData;
    private Map<String, Field> fieldMap;

    private byte[] dataBytes;
    private int headerLength;
    private int messageLength;
    private boolean hasBitmap;
    private int totalLengthLength;
    private List<Field> fieldConfigList;

    private Iso8583(int length, byte[] header, byte[] msgType, String bitmap, byte[] allFieldData, Map<String, Field> fieldMap) {
        this.length = length;
        this.header = header;
        this.bitmap = bitmap;
        this.msgType = msgType;
        this.fieldMap = fieldMap;
        this.allFieldData = allFieldData;
    }

    private Iso8583(int totalLengthLength, byte[] header, byte[] msgType, boolean hasBitmap, Map<String, Field> fieldMap) {
        this.header = header;
        this.msgType = msgType;
        this.fieldMap = fieldMap;
        this.hasBitmap = hasBitmap;
        this.totalLengthLength = totalLengthLength;
    }

    private Iso8583(byte[] dataBytes, int totalLengthLength, int headerLength, int messageLength, List<Field> fieldConfigList) {
        this.dataBytes = dataBytes;
        this.headerLength = headerLength;
        this.messageLength = messageLength;
        this.fieldConfigList = fieldConfigList;
        this.totalLengthLength = totalLengthLength;
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

    public Iso8583 deepCopy() {
        return this;
    }

    public byte[] encode() throws Exception {
        if (fieldMap == null || fieldMap.size() == 0) {
            throw new Exception("Data error, fieldMap cannot be null");
        }
        return Iso8583Encode.encode(fieldMap, totalLengthLength, header, msgType, hasBitmap);
    }

    public Iso8583 decode() throws Exception {
        Map<String, Field> fieldConfigMap = BitmapHelper.getDefaultFieldConfig();
        if (fieldConfigList != null && fieldConfigList.size() > 0) {
            for (int i = 0; i < fieldConfigList.size(); i++) {
                Field field = fieldConfigList.get(i);
                int position = field.getPosition();
                if (position >= 0 && position <= 128) {
                    String name = Iso8583Helper.getMessageFieldName(position);
                    fieldConfigMap.put(name, field);
                } else {
                    Logger.getLogger(Iso8583Config.TAG).log(Level.WARNING, "Field [" + i + "] out of configuration range.");
                }
            }
        }
        return decode(fieldConfigMap);
    }

    public Iso8583 decode(Map<String, Field> fieldConfigMap) throws Exception {
        if (dataBytes == null || dataBytes.length == 0) {
            throw new Exception("Data error, dataBytes cannot be null");
        }
        return Iso8583Decode.decode(dataBytes, totalLengthLength, headerLength, messageLength, fieldConfigMap);
    }

    public static final class Builder {

        private int length;
        private byte[] header;
        private String bitmap;
        private byte[] message;
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

        public Builder addMessage(byte[] bytes) {
            this.message = bytes;
            return this;
        }

        public Builder addBitmap(String bitmap) {
            this.bitmap = bitmap;
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

        public Builder addMessageField(Field messageField) throws Exception {
            int position = messageField.getPosition();
            String name = Iso8583Helper.getMessageFieldName(position);
            Field field = BitmapHelper.getMessageFieldClone(name);
            if (field != null) {
                fieldMap.put(name, messageField);
            } else {
                throw new Exception("MessageField can only be between 1 - 128 => " + position);
            }
            return this;
        }

        public Iso8583 build() {
            return new Iso8583(length, header, message, bitmap, allFieldData, fieldMap);
        }

    }

    public static final class EncodeBuilder {

        private byte[] header;
        private byte[] msgType;
        private boolean hasBitmap = true;
        private int totalSizeLength = 2;
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

        public EncodeBuilder addField(int position, String value) throws Exception {
            String name = Iso8583Helper.getMessageFieldName(position);
            Field field = BitmapHelper.getMessageFieldClone(name);
            if (field != null) {
                field.setDataString(value);
                fieldMap.put(name, field);
            } else {
                throw new Exception("Field can only be between 1 - 128 => " + position);
            }
            return this;
        }

        public EncodeBuilder addField(Field messageField) throws Exception {
            int position = messageField.getPosition();
            String name = Iso8583Helper.getMessageFieldName(position);
            Field field = BitmapHelper.getMessageFieldClone(name);
            if (field != null) {
                fieldMap.put(name, messageField);
            } else {
                throw new Exception("Field can only be between 1 - 128 => " + position);
            }
            return this;
        }

        public EncodeBuilder hasBitmap(boolean hasBitmap) {
            this.hasBitmap = hasBitmap;
            return this;
        }

        public EncodeBuilder addTotalLengthLength(int totalLengthLength) {
            this.totalSizeLength = totalLengthLength;
            return this;
        }

        public Iso8583 build() {
            return new Iso8583(totalSizeLength, header, msgType, hasBitmap, fieldMap);
        }

    }

    public static final class DecodeBuilder {

        private byte[] dataBytes;
        private int headerLength = 0;
        private int messageLength = 2;
        private int totalLengthLength = 2;
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

        public DecodeBuilder addTotalLengthLength(int totalLengthLength) {
            this.totalLengthLength = totalLengthLength;
            return this;
        }

        public DecodeBuilder addFieldConfigList(List<Field> fieldConfigList) {
            this.fieldConfigList = fieldConfigList;
            return this;
        }

        public Iso8583 build() {
            return new Iso8583(dataBytes, totalLengthLength, headerLength, messageLength, fieldConfigList);
        }

    }

}