package com.light.encode.ios8583;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public final class Field implements Serializable, Cloneable {

    private static final long serialVersionUID = -6585924538118510224L;

    /**
     * which domain, value: 1- 128
     * {@link Constant.Position}
     */
    private int position;

    /**
     * when aligning, insufficient fill characters
     */
    private String padding = "0";

    /**
     * data alignment type
     * use for const length filed: RIGHT  LEFT (主要定长域使用) 左右靠
     * {@link Constant.AlignType}
     */
    private String alignType = Constant.AlignType.LEFT;

    /**
     * whether the data length is variable, value: NONE / PAIR / TRIPE
     * data type of length: ASC / HEX / BCD (default) (只有变长域才存在)  - ASC HEX BCD(不填则默认BCD)
     * {@link Constant.LengthType}
     */
    private String lengthType = Constant.LengthType.NONE;

    /**
     * data length type, BCD / ASCII
     * {@link Constant.EncodeType}
     */
    private String lengthEncode = Constant.EncodeType.BCD;

    /**
     * 0: length fixed
     * 2: length of data length is 1 表示可变长域长度用1个字节表示
     * 3: length of data length is 2 表示可变长域长度用2个字节表示
     */
    private int dataLength;

    private byte[] dataBytes;
    private String dataString;

    /**
     * data type, value: BCD / ASCII / BIT
     * BCD: num in right, append 0 left 数值, 右靠, 首位有效数字前填充零。若表示金额, 则最右两位表示角分(在国内使用, 默认压缩为BCD码, 所以处理上和Z相同)
     * ASC: character in left, append blank right(A, AN, ANS, AS) 字母, 数字和/或特殊符号, 左靠, 右部多余部分填空格, 包括 A, AN, ANS, AS
     * BIT: hex 格式, 原始数据
     * {@link Constant.EncodeType}
     */
    private String dataEncode = Constant.EncodeType.BCD;

    /**
     * 域描述
     */
    private String desc;

    private Field() {
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

    public int getAlignType() {
        switch (alignType) {
            case Constant.AlignType.LEFT:
                return Helper.ALIGN_LEFT;
            case Constant.AlignType.RIGHT:
                return Helper.ALIGN_RIGHT;
        }
        return Helper.ALIGN_LEFT;
    }

    public void setAlignType(String alignType) {
        this.alignType = alignType;
    }

    public int getLengthType() {
        switch (lengthType) {
            case Constant.LengthType.NONE:
                return Helper.LENGTH_VAR_NONE;
            case Constant.LengthType.PAIR:
                return Helper.LENGTH_VAR_PAIR;
            case Constant.LengthType.TRIP:
                return Helper.LENGTH_VAR_TRIP;
        }
        return Helper.LENGTH_VAR_NONE;
    }

    public void setLengthType(String lengthType) {
        this.lengthType = lengthType;
    }

    public void setLengthEncode(String lengthEncode) {
        this.lengthEncode = lengthEncode;
    }

    public int getLengthEncode() {
        switch (lengthEncode) {
            case Constant.EncodeType.BCD:
                return Helper.ENCODE_BCD;
            case Constant.EncodeType.BIT:
                return Helper.ENCODE_BIT;
            case Constant.EncodeType.ASC:
                return Helper.ENCODE_ASC;
        }
        return Helper.ENCODE_BCD;
    }

    public int getDataEncode() {
        switch (dataEncode) {
            case Constant.EncodeType.BCD:
                return Helper.ENCODE_BCD;
            case Constant.EncodeType.BIT:
                return Helper.ENCODE_BIT;
            case Constant.EncodeType.ASC:
                return Helper.ENCODE_ASC;
        }
        return Helper.ENCODE_BCD;
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
        int lengthType = getLengthType();
        int dataEncode = getDataEncode();
        if (lengthType > Helper.LENGTH_VAR_NONE && dataBytes != null) {
            if (dataEncode == Helper.ENCODE_BCD) {
                setDataLength(dataBytes.length * 2);
            } else if (dataEncode == Helper.ENCODE_BIT) {
                // BIT 域长度以字节为单位；十六进制字符串才是每字节两个字符。
                setDataLength(dataBytes.length);
            } else if (dataEncode == Helper.ENCODE_ASC) {
                setDataLength(dataBytes.length);
            }
        }
    }

    public String getDataString() {
        return dataString;
    }

    public void setDataString(String dataString) {
        this.dataString = dataString;
        int dataEncode = getDataEncode();
        int lengthType = getLengthType();
        if (lengthType > Helper.LENGTH_VAR_NONE && dataString != null) {
            int length = dataString.length();
            if (dataEncode == Helper.ENCODE_BCD) {
                // BCD 变长域的长度头表示数字位数，而不是压缩后的字节数。
                setDataLength(length);
            } else if (dataEncode == Helper.ENCODE_BIT) {
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

    public Field cloneField() {
        try {
            Field clone = (Field) super.clone();
            clone.dataBytes = dataBytes == null ? null : dataBytes.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("clone Field failed", e);
        }
    }

    public static final class Builder {

        private final Map<String, Object> map = new HashMap<>();

        public Builder position(int position) {
            map.put(Constant.Field.POSITION, position);
            return this;
        }

        public Builder padding(String padding) {
            map.put(Constant.Field.PADDING, padding);
            return this;
        }

        public Builder alignType(String alignType) {
            map.put(Constant.Field.ALIGN_TYPE, alignType);
            return this;
        }

        public Builder lengthEncode(String lengthType) {
            map.put(Constant.Field.LENGTH_ENCODE, lengthType);
            return this;
        }

        public Builder lengthType(String lengthType) {
            map.put(Constant.Field.LENGTH_TYPE, lengthType);
            return this;
        }

        public Builder dataEncode(String dataEncode) {
            map.put(Constant.Field.DATA_ENCODE, dataEncode);
            return this;
        }

        public Builder dataLength(int dataLength) {
            map.put(Constant.Field.DATA_LENGTH, dataLength);
            return this;
        }

        public Builder dataBytes(byte[] dataBytes) {
            map.put(Constant.Field.DATA_BYTES, dataBytes);
            return this;
        }

        public Builder dataString(String dataString) {
            map.put(Constant.Field.DATA_STRING, dataString);
            return this;
        }

        public Builder desc(String desc) {
            map.put(Constant.Field.DESC, desc);
            return this;
        }

        public Field build() {
            Field field = new Field();
            Object object = map.get(Constant.Field.POSITION);
            if (object != null) {
                int position = (int) object;
                String fieldName = Helper.getFieldName(position);
                Field clone = Helper.getFieldClone(fieldName);
                if (clone != null) {
                    field = clone;
                } else {
                    field.setPosition(position);
                }
            }
            // 数据长度依赖编码方式，必须先应用配置，再写入数据。
            object = map.get(Constant.Field.DATA_ENCODE);
            if (object != null) {
                String dataEncode = (String) object;
                field.setDataEncode(dataEncode);
            }
            object = map.get(Constant.Field.PADDING);
            if (object != null) {
                String padding = (String) object;
                field.setPadding(padding);
            }
            object = map.get(Constant.Field.ALIGN_TYPE);
            if (object != null) {
                String alignType = (String) object;
                field.setAlignType(alignType);
            }
            object = map.get(Constant.Field.LENGTH_TYPE);
            if (object != null) {
                String lengthType = (String) object;
                field.setLengthType(lengthType);
            }
            object = map.get(Constant.Field.LENGTH_ENCODE);
            if (object != null) {
                String lengthEncode = (String) object;
                field.setLengthEncode(lengthEncode);
            }
            object = map.get(Constant.Field.DESC);
            if (object != null) {
                String desc = (String) object;
                field.setDesc(desc);
            }
            object = map.get(Constant.Field.DATA_BYTES);
            if (object != null) {
                field.setDataBytes((byte[]) object);
            }
            object = map.get(Constant.Field.DATA_STRING);
            if (object != null) {
                field.setDataString((String) object);
            }
            // 调用方显式指定的长度最终生效，适用于定长域和特殊协议。
            object = map.get(Constant.Field.DATA_LENGTH);
            if (object != null) {
                field.setDataLength((int) object);
            }
            return field;
        }

    }

}
