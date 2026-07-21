package com.light.encode.ios8583;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 一个 ISO8583 数据域的“定义 + 当前值”。
 *
 * <p>配置加载后，每次组包/解包都会使用配置的副本，因此写入 {@link #dataString} 或
 * {@link #dataBytes} 不会污染全局模板。位置 0 表示 MTI，位置 1 为位图保留位，普通数据域
 * 使用 2～128。</p>
 *
 * <p><strong>长度单位：</strong>BCD 域的 {@link #dataLength} 表示数字位数；ASC 和 BIT 域
 * 表示字节数。对于变长域，写入数据时会自动刷新实际长度。</p>
 */
public final class Iso8583Field implements Serializable, Cloneable {

    private static final long serialVersionUID = -6585924538118510224L;

    /** 字段位置：0 为 MTI，1 为位图保留位，2～128 为数据域。 */
    private int position;

    /** 定长域不足时使用的单个十六进制填充字符，默认 {@code 0}。 */
    private String padding = "0";

    /** 定长域的对齐方式，取值见 {@link Iso8583Constant.AlignType}。 */
    private String alignType = Iso8583Constant.AlignType.LEFT;

    /** 长度类型：定长、LLVAR 或 LLLVAR，取值见 {@link Iso8583Constant.LengthType}。 */
    private String lengthType = Iso8583Constant.LengthType.NONE;

    /** 变长域长度头的编码方式，仅支持 BCD 或 ASC。 */
    private String lengthEncode = Iso8583Constant.EncodeType.BCD;

    /**
     * 定长域的声明长度，或变长域当前值的实际长度。
     * BCD 按数字位数计，ASC/BIT 按字节数计。
     */
    private int dataLength;

    private byte[] dataBytes;
    private String dataString;

    /**
     * 字段值编码：BCD 为压缩数字/十六进制半字节，ASC 为 US-ASCII，BIT 为原始字节
     * （字符串输入时使用十六进制形式）。
     */
    private String dataEncode = Iso8583Constant.EncodeType.BCD;

    /** 仅用于调试展示的字段说明，不参与报文编码。 */
    private String desc;

    private Iso8583Field() {
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getPadding() {
        return padding;
    }

    public void setPadding(String padding) {
        this.padding = padding;
    }

    public String getAlignType() {
        return alignType;
    }

    int alignCode() {
        switch (alignType) {
            case Iso8583Constant.AlignType.LEFT:
                return Iso8583FieldSupport.ALIGN_LEFT;
            case Iso8583Constant.AlignType.RIGHT:
                return Iso8583FieldSupport.ALIGN_RIGHT;
        }
        return Iso8583FieldSupport.ALIGN_LEFT;
    }

    public void setAlignType(String alignType) {
        this.alignType = alignType;
    }

    public String getLengthType() {
        return lengthType;
    }

    int lengthTypeCode() {
        switch (lengthType) {
            case Iso8583Constant.LengthType.NONE:
                return Iso8583FieldSupport.LENGTH_VAR_NONE;
            case Iso8583Constant.LengthType.LLVAR:
                return Iso8583FieldSupport.LENGTH_LLVAR;
            case Iso8583Constant.LengthType.LLLVAR:
                return Iso8583FieldSupport.LENGTH_LLLVAR;
        }
        return Iso8583FieldSupport.LENGTH_VAR_NONE;
    }

    public void setLengthType(String lengthType) {
        this.lengthType = lengthType;
    }

    public void setLengthEncode(String lengthEncode) {
        this.lengthEncode = lengthEncode;
    }

    public String getLengthEncode() {
        return lengthEncode;
    }

    int lengthEncodingCode() {
        switch (lengthEncode) {
            case Iso8583Constant.EncodeType.BCD:
                return Iso8583FieldSupport.ENCODE_BCD;
            case Iso8583Constant.EncodeType.BIT:
                return Iso8583FieldSupport.ENCODE_BIT;
            case Iso8583Constant.EncodeType.ASCII:
                return Iso8583FieldSupport.ENCODE_ASCII;
        }
        return Iso8583FieldSupport.ENCODE_BCD;
    }

    public String getDataEncode() {
        return dataEncode;
    }

    int dataEncodingCode() {
        switch (dataEncode) {
            case Iso8583Constant.EncodeType.BCD:
                return Iso8583FieldSupport.ENCODE_BCD;
            case Iso8583Constant.EncodeType.BIT:
                return Iso8583FieldSupport.ENCODE_BIT;
            case Iso8583Constant.EncodeType.ASCII:
                return Iso8583FieldSupport.ENCODE_ASCII;
        }
        return Iso8583FieldSupport.ENCODE_BCD;
    }

    public void setDataEncode(String dataEncode) {
        this.dataEncode = dataEncode;
    }

    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public byte[] getDataBytes() {
        return dataBytes == null ? null : dataBytes.clone();
    }

    public void setDataBytes(byte[] dataBytes) {
        this.dataBytes = dataBytes == null ? null : dataBytes.clone();
        // 变长域必须以本次实际值生成长度头，不能沿用 XML 中声明的最大长度。
        int lengthType = lengthTypeCode();
        int dataEncode = dataEncodingCode();
        if (lengthType > Iso8583FieldSupport.LENGTH_VAR_NONE && dataBytes != null) {
            if (dataEncode == Iso8583FieldSupport.ENCODE_BCD) {
                setDataLength(dataBytes.length * 2);
            } else if (dataEncode == Iso8583FieldSupport.ENCODE_BIT) {
                // BIT 域长度以字节为单位；十六进制字符串才是每字节两个字符。
                setDataLength(dataBytes.length);
            } else if (dataEncode == Iso8583FieldSupport.ENCODE_ASCII) {
                setDataLength(dataBytes.length);
            }
        }
    }

    public String getDataString() {
        return dataString;
    }

    public void setDataString(String dataString) {
        this.dataString = dataString;
        // 字符串形式的 BIT 数据是十六进制，所以两个字符表示一个字节。
        int dataEncode = dataEncodingCode();
        int lengthType = lengthTypeCode();
        if (lengthType > Iso8583FieldSupport.LENGTH_VAR_NONE && dataString != null) {
            int length = dataString.length();
            if (dataEncode == Iso8583FieldSupport.ENCODE_BCD) {
                // BCD 变长域的长度头表示数字位数，而不是压缩后的字节数。
                setDataLength(length);
            } else if (dataEncode == Iso8583FieldSupport.ENCODE_BIT) {
                setDataLength(length / 2 + length % 2);
            } else {
                setDataLength(length);
            }
        }
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    /** 返回字段的独立副本，包含防御性复制后的字节值。 */
    public Iso8583Field copy() {
        try {
            Iso8583Field clone = (Iso8583Field) super.clone();
            clone.dataBytes = dataBytes == null ? null : dataBytes.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("clone Iso8583Field failed", e);
        }
    }

    /** @deprecated 使用 {@link #copy()}。 */
    @Deprecated
    public Iso8583Field cloneField() {
        return copy();
    }

    /**
     * 字段构建器。若已加载相同位置的全局配置，将从配置模板克隆后再覆盖显式参数。
     */
    public static final class Builder {

        private final Map<String, Object> map = new HashMap<>();

        public Builder position(int position) {
            map.put(Iso8583Constant.XmlField.POSITION, position);
            return this;
        }

        public Builder padding(String padding) {
            map.put(Iso8583Constant.XmlField.PADDING, padding);
            return this;
        }

        public Builder alignType(String alignType) {
            map.put(Iso8583Constant.XmlField.ALIGN_TYPE, alignType);
            return this;
        }

        public Builder lengthEncode(String lengthEncode) {
            map.put(Iso8583Constant.XmlField.LENGTH_ENCODE, lengthEncode);
            return this;
        }

        public Builder lengthType(String lengthType) {
            map.put(Iso8583Constant.XmlField.LENGTH_TYPE, lengthType);
            return this;
        }

        public Builder dataEncode(String dataEncode) {
            map.put(Iso8583Constant.XmlField.DATA_ENCODE, dataEncode);
            return this;
        }

        public Builder dataLength(int dataLength) {
            map.put(Iso8583Constant.XmlField.DATA_LENGTH, dataLength);
            return this;
        }

        public Builder dataBytes(byte[] dataBytes) {
            map.put(Iso8583Constant.XmlField.DATA_BYTES, dataBytes);
            return this;
        }

        public Builder dataString(String dataString) {
            map.put(Iso8583Constant.XmlField.DATA_STRING, dataString);
            return this;
        }

        public Builder desc(String desc) {
            map.put(Iso8583Constant.XmlField.DESC, desc);
            return this;
        }

        public Iso8583Field build() {
            Iso8583Field field = new Iso8583Field();
            Object object = map.get(Iso8583Constant.XmlField.POSITION);
            if (object != null) {
                int position = (int) object;
                String fieldName = Iso8583FieldSupport.fieldName(position);
                Iso8583Field clone = Iso8583FieldSupport.fieldTemplateCopy(fieldName);
                if (clone != null) {
                    field = clone;
                } else {
                    field.setPosition(position);
                }
            }
            // 数据长度依赖编码方式，必须先应用配置，再写入数据。
            object = map.get(Iso8583Constant.XmlField.DATA_ENCODE);
            if (object != null) {
                String dataEncode = (String) object;
                field.setDataEncode(dataEncode);
            }
            object = map.get(Iso8583Constant.XmlField.PADDING);
            if (object != null) {
                String padding = (String) object;
                field.setPadding(padding);
            }
            object = map.get(Iso8583Constant.XmlField.ALIGN_TYPE);
            if (object != null) {
                String alignType = (String) object;
                field.setAlignType(alignType);
            }
            object = map.get(Iso8583Constant.XmlField.LENGTH_TYPE);
            if (object != null) {
                String lengthType = (String) object;
                field.setLengthType(lengthType);
            }
            object = map.get(Iso8583Constant.XmlField.LENGTH_ENCODE);
            if (object != null) {
                String lengthEncode = (String) object;
                field.setLengthEncode(lengthEncode);
            }
            object = map.get(Iso8583Constant.XmlField.DESC);
            if (object != null) {
                String desc = (String) object;
                field.setDesc(desc);
            }
            object = map.get(Iso8583Constant.XmlField.DATA_BYTES);
            if (object != null) {
                field.setDataBytes((byte[]) object);
            }
            object = map.get(Iso8583Constant.XmlField.DATA_STRING);
            if (object != null) {
                field.setDataString((String) object);
            }
            // 调用方显式指定的长度最终生效，适用于定长域和特殊协议。
            object = map.get(Iso8583Constant.XmlField.DATA_LENGTH);
            if (object != null) {
                field.setDataLength((int) object);
            }
            return field;
        }

    }

}
