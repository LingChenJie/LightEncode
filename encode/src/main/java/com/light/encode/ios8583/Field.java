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
    private String padding;

    /**
     * data alignment type
     * use for const length filed: RIGHT  LEFT (主要定长域使用) 左右靠
     * {@link Constant.AlignType}
     */
    private String alignType;

    /**
     * whether the data length is variable, value: NONE / PAIR / TRIPE
     * data type of length: ASC / HEX / BCD (default) (只有变长域才存在)  - ASC HEX BCD(不填则默认BCD)
     * {@link Constant.LengthType}
     */
    private String lengthType;

    /**
     * data length type, BCD / ASCII
     * {@link Constant.EncodeType}
     */
    private String lengthEncode;

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
    private String dataEncode;

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
        return dataBytes;
    }

    public void setDataBytes(byte[] dataBytes) {
        this.dataBytes = dataBytes;
        int lengthType = getLengthType();
        int dataEncode = getDataEncode();
        if (lengthType > Helper.LENGTH_VAR_NONE && dataBytes != null) {
            if (dataEncode == Helper.ENCODE_BCD) {
                setDataLength(dataBytes.length * 2);
            } else if (dataEncode == Helper.ENCODE_BIT) {
                setDataLength(dataBytes.length * 2);
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
        int lengthType = getLengthType();
        if (lengthType > Helper.LENGTH_VAR_NONE && dataString != null) {
            int length = dataString.length();
            setDataLength(length);
        }
    }

    public Field cloneField() {
        try {
            return (Field) super.clone();
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("clone Field failed");
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
            object = map.get(Constant.Field.DATA_BYTES);
            if (object != null) {
                byte[] dataBytes = (byte[]) object;
                field.setDataBytes(dataBytes);
            }
            object = map.get(Constant.Field.DATA_STRING);
            if (object != null) {
                String dataString = (String) object;
                field.setDataString(dataString);
            }
            object = map.get(Constant.Field.DATA_LENGTH);
            if (object != null) {
                int dataLength = (int) object;
                field.setDataLength(dataLength);
            }
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
            return field;
        }

    }

}