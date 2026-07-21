package com.light.encode.ios8583;

import com.light.encode.util.ByteUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * ISO8583 报文及编解码门面。
 *
 * <p>使用 {@link EncodeBuilder} 创建待组包对象并调用 {@link #encode()}；使用
 * {@link DecodeBuilder} 创建待解析对象并调用 {@link #decode()}。解包结果包含长度、报文头、
 * MTI、位图和按 {@code Fnnn} 命名的数据域。</p>
 *
 * <p>本类只处理字节报文，不包含网络传输、PIN 加密、MAC 计算或密钥管理。</p>
 */
public final class Iso8583Message implements Serializable {

    private static final long serialVersionUID = -6514454101496082018L;

    private int length; // 长度头解析出的报文体字节数，不包含长度头自身。
    private byte[] header;
    private String bitmap;
    private String msgType;
    private byte[] allFieldData; // 位图及位图之后的所有数据域字节。
    private Map<String, Iso8583Field> fieldMap;

    private byte[] dataBytes;
    private int headerLength;
    private boolean hasBitmap;
    private int lengthLength; // 报文总长度头所占字节数，0 表示无长度头。
    private List<Iso8583Field> fieldConfigList;

    private Iso8583Message(int length, byte[] header, String msgType, String bitmap, byte[] allFieldData, Map<String, Iso8583Field> fieldMap) {
        this.length = length;
        this.header = copy(header);
        this.msgType = msgType;
        this.bitmap = bitmap;
        this.allFieldData = copy(allFieldData);
        this.fieldMap = copyFields(fieldMap);
    }

    private Iso8583Message(int lengthLength, byte[] header, String msgType, boolean hasBitmap, Map<String, Iso8583Field> fieldMap) {
        this.lengthLength = lengthLength;
        this.header = copy(header);
        this.msgType = msgType;
        this.hasBitmap = hasBitmap;
        this.fieldMap = copyFields(fieldMap);
    }

    private Iso8583Message(byte[] dataBytes, int lengthLength, int headerLength, List<Iso8583Field> fieldConfigList) {
        this.dataBytes = copy(dataBytes);
        this.lengthLength = lengthLength;
        this.headerLength = headerLength;
        this.fieldConfigList = fieldConfigList;
    }


    /**
     * 按当前字段配置生成完整报文字节。
     *
     * @return 长度头（可选）+ Header（可选）+ MTI + Bitmap（可选）+ 数据域
     * @throws IllegalStateException 未添加任何字段时
     * @throws IllegalArgumentException 字段值、长度或编码配置不合法时
     */
    public byte[] encode() {
        if (fieldMap == null || fieldMap.isEmpty()) {
            throw new IllegalStateException("Data error, fieldMap cannot be null");
        }
        return Iso8583Encoder.encode(fieldMap, lengthLength, header, msgType, hasBitmap);
    }

    /**
     * 使用 {@link Iso8583Config} 中的全局字段模板及本 Builder 的覆盖配置解包。
     */
    public Iso8583Message decode() {
        Map<String, Iso8583Field> fieldConfigMap = Iso8583FieldSupport.configSnapshot();
        if (fieldConfigList != null && !fieldConfigList.isEmpty()) {
            for (int i = 0; i < fieldConfigList.size(); i++) {
                Iso8583Field field = fieldConfigList.get(i);
                int position = field.getPosition();
                if (position >= 0 && position <= 128) {
                    String name = Iso8583FieldSupport.fieldName(position);
                    fieldConfigMap.put(name, field.copy());
                } else {
                    throw new IllegalArgumentException("Iso8583Field [" + i + "] out of configuration range.");
                }
            }
        }
        return decode(fieldConfigMap);
    }

    /** 使用调用方提供的字段配置表解包，不读取全局字段配置。 */
    public Iso8583Message decode(Map<String, Iso8583Field> fieldConfigMap) {
        if (dataBytes == null || dataBytes.length == 0) {
            throw new IllegalStateException("Data error, dataBytes cannot be null");
        }
        if (fieldConfigMap == null || fieldConfigMap.isEmpty()) {
            throw new IllegalArgumentException("fieldConfigMap cannot be empty");
        }
        Map<String, Iso8583Field> workingConfig = new HashMap<>();
        for (Map.Entry<String, Iso8583Field> entry : fieldConfigMap.entrySet()) {
            Iso8583Field field = entry.getValue();
            if (field == null) {
                throw new IllegalArgumentException("Iso8583Field config cannot contain null values");
            }
            workingConfig.put(entry.getKey(), field.copy());
        }
        return Iso8583Decoder.decode(dataBytes, lengthLength, headerLength, workingConfig);
    }

    /** 仅供解包器组装不可见的中间结果。 */
    static final class ResultBuilder {

        private int length;
        private byte[] header;
        private String bitmap;
        private String msgType;
        private byte[] allFieldData;
        private final Map<String, Iso8583Field> fieldMap = new HashMap<>();

        ResultBuilder length(int length) {
            this.length = length;
            return this;
        }

        ResultBuilder header(byte[] bytes) {
            this.header = bytes;
            return this;
        }

        ResultBuilder bitmap(String bitmap) {
            this.bitmap = bitmap;
            return this;
        }

        ResultBuilder messageType(String bytes) {
            this.msgType = bytes;
            return this;
        }

        ResultBuilder allFieldData(byte[] bytes) {
            this.allFieldData = bytes;
            return this;
        }

        ResultBuilder fields(Map<String, Iso8583Field> map) {
            fieldMap.putAll(map);
            return this;
        }

        Iso8583Message build() {
            return new Iso8583Message(length, header, msgType, bitmap, allFieldData, fieldMap);
        }

    }

    /** 组包参数构建器。默认包含位图，默认报文长度头占 2 字节。 */
    public static final class EncodeBuilder {

        private byte[] header;
        private String msgType;
        private boolean hasBitmap = true;
        private int lengthLength = 2;
        private final Map<String, Iso8583Field> fieldMap = new HashMap<>();

        /** 设置原始 Header/TPDU 字节。 */
        public EncodeBuilder header(byte[] bytes) {
            this.header = copy(bytes);
            return this;
        }

        /** 添加十六进制形式的渠道头/TPDU，例如 {@code 6001010000}。 */
        public EncodeBuilder header(String hexString) {
            this.header = ByteUtil.hexString2Bytes(hexString);
            return this;
        }

        /** 添加 MTI；实际编码由 position=0 的字段配置决定。 */
        public EncodeBuilder messageType(String value) {
            field(0, value);
            return this;
        }

        /** 批量添加已经构建好的字段。 */
        public EncodeBuilder fields(Map<String, Iso8583Field> map) {
            fieldMap.putAll(map);
            return this;
        }

        /**
         * 从全局配置克隆指定数据域并写入字符串值。
         * BCD/BIT 值使用十六进制字符串，ASC 值使用普通文本。
         */
        public EncodeBuilder field(int position, String value) {
            if (position == 0) {
                this.msgType = value;
            }
            String name = Iso8583FieldSupport.fieldName(position);
            Iso8583Field field = Iso8583FieldSupport.fieldTemplateCopy(name);
            if (field != null) {
                field.setDataString(value);
                fieldMap.put(name, field);
            } else {
                throw new IllegalArgumentException("bitmap does not configure the field for position " + position);
            }
            return this;
        }

        public EncodeBuilder field(Iso8583Field field) {
            int position = field.getPosition();
            String name = Iso8583FieldSupport.fieldName(position);
            fieldMap.put(name, field);
            return this;
        }

        public EncodeBuilder bitmapEnabled(boolean hasBitmap) {
            this.hasBitmap = hasBitmap;
            return this;
        }

        /** 设置大端二进制报文长度头的字节数；传 0 表示不生成长度头。 */
        public EncodeBuilder lengthHeaderBytes(int lengthLength) {
            this.lengthLength = lengthLength;
            return this;
        }

        /** @deprecated 使用 {@link #header(byte[])}。 */
        @Deprecated
        public EncodeBuilder addHeader(byte[] bytes) {
            return header(bytes);
        }

        /** @deprecated 使用 {@link #header(String)}。 */
        @Deprecated
        public EncodeBuilder addHeader(String hexString) {
            return header(hexString);
        }

        /** @deprecated 使用 {@link #messageType(String)}。 */
        @Deprecated
        public EncodeBuilder addMsgType(String value) {
            return messageType(value);
        }

        /** @deprecated 使用 {@link #fields(Map)}。 */
        @Deprecated
        public EncodeBuilder addField(Map<String, Iso8583Field> map) {
            return fields(map);
        }

        /** @deprecated 使用 {@link #field(int, String)}。 */
        @Deprecated
        public EncodeBuilder addField(int position, String value) {
            return field(position, value);
        }

        /** @deprecated 使用 {@link #field(Iso8583Field)}。 */
        @Deprecated
        public EncodeBuilder addField(Iso8583Field field) {
            return field(field);
        }

        /** @deprecated 使用 {@link #bitmapEnabled(boolean)}。 */
        @Deprecated
        public EncodeBuilder hasBitmap(boolean hasBitmap) {
            return bitmapEnabled(hasBitmap);
        }

        /** @deprecated 使用 {@link #lengthHeaderBytes(int)}。 */
        @Deprecated
        public EncodeBuilder addLengthLength(int lengthLength) {
            return lengthHeaderBytes(lengthLength);
        }

        public Iso8583Message build() {
            return new Iso8583Message(lengthLength, header, msgType, hasBitmap, fieldMap);
        }

    }

    /** 解包参数构建器。默认报文头为 0 字节，长度头为 2 字节。 */
    public static final class DecodeBuilder {

        private byte[] dataBytes;
        private int headerLength = 0;
        private int lengthLength = 2;
        private List<Iso8583Field> fieldListConfig;

        /** 设置待解包的完整报文字节。 */
        public DecodeBuilder data(byte[] dataBytes) {
            this.dataBytes = copy(dataBytes);
            return this;
        }

        /** 设置 Header/TPDU 的实际字节数。 */
        public DecodeBuilder headerLength(int headerLength) {
            this.headerLength = headerLength;
            return this;
        }

        /** 设置大端二进制报文长度头的字节数；传 0 表示输入不含长度头。 */
        public DecodeBuilder lengthHeaderBytes(int lengthLength) {
            this.lengthLength = lengthLength;
            return this;
        }

        /** 添加仅对本次解包生效的字段配置，可覆盖全局同位置配置。 */
        public DecodeBuilder fieldConfigs(List<Iso8583Field> fieldList) {
            if (fieldListConfig == null) {
                fieldListConfig = new ArrayList<>();
            }
            this.fieldListConfig.addAll(fieldList);
            return this;
        }

        public DecodeBuilder fieldConfig(Iso8583Field field) {
            if (fieldListConfig == null) {
                fieldListConfig = new ArrayList<>();
            }
            fieldListConfig.add(field);
            return this;
        }

        /** @deprecated 使用 {@link #data(byte[])}。 */
        @Deprecated
        public DecodeBuilder addDataBytes(byte[] dataBytes) {
            return data(dataBytes);
        }

        /** @deprecated 使用 {@link #headerLength(int)}。 */
        @Deprecated
        public DecodeBuilder addHeaderLength(int headerLength) {
            return headerLength(headerLength);
        }

        /** @deprecated 使用 {@link #lengthHeaderBytes(int)}。 */
        @Deprecated
        public DecodeBuilder addLengthLength(int lengthLength) {
            return lengthHeaderBytes(lengthLength);
        }

        /** @deprecated 使用 {@link #fieldConfigs(List)}。 */
        @Deprecated
        public DecodeBuilder addFieldListConfig(List<Iso8583Field> fieldList) {
            return fieldConfigs(fieldList);
        }

        /** @deprecated 使用 {@link #fieldConfig(Iso8583Field)}。 */
        @Deprecated
        public DecodeBuilder addFieldConfig(Iso8583Field field) {
            return fieldConfig(field);
        }

        public Iso8583Message build() {
            return new Iso8583Message(dataBytes, lengthLength, headerLength, fieldListConfig);
        }

    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getHeader() {
        return copy(header);
    }

    public void setHeader(byte[] header) {
        this.header = copy(header);
    }

    public String getBitmap() {
        return bitmap;
    }

    public void setBitmap(String bitmap) {
        this.bitmap = bitmap;
    }

    public String getMessageType() {
        return msgType;
    }

    public void setMessageType(String msgType) {
        this.msgType = msgType;
    }

    /** @deprecated 使用 {@link #getMessageType()}。 */
    @Deprecated
    public String getMsgType() {
        return getMessageType();
    }

    /** @deprecated 使用 {@link #setMessageType(String)}。 */
    @Deprecated
    public void setMsgType(String msgType) {
        setMessageType(msgType);
    }

    public byte[] getAllFieldData() {
        return copy(allFieldData);
    }

    public void setAllFieldData(byte[] allFieldData) {
        this.allFieldData = copy(allFieldData);
    }

    public Map<String, Iso8583Field> getFieldMap() {
        return copyFields(fieldMap);
    }

    public void setFieldMap(Map<String, Iso8583Field> fieldMap) {
        this.fieldMap = copyFields(fieldMap);
    }

    private static byte[] copy(byte[] bytes) {
        return bytes == null ? null : bytes.clone();
    }

    private static Map<String, Iso8583Field> copyFields(Map<String, Iso8583Field> fields) {
        TreeMap<String, Iso8583Field> copy = new TreeMap<>();
        if (fields != null) {
            for (Map.Entry<String, Iso8583Field> entry : fields.entrySet()) {
                Iso8583Field field = entry.getValue();
                copy.put(entry.getKey(), field == null ? null : field.copy());
            }
        }
        return copy;
    }

}
