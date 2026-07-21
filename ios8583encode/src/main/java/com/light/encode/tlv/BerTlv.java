package com.light.encode.tlv;

import com.light.encode.util.ByteUtil;

import java.io.Serializable;

/**
 * 一个 BER-TLV 元素。Tag 和 Value 均使用不带空格的十六进制字符串，Length 使用字节数。
 */
@SuppressWarnings("unused")
public final class BerTlv implements Serializable {

    private static final long serialVersionUID = -5884698039382212843L;

    private final int length;
    private final String tag;
    private final String value;

    public BerTlv(String tag, String value) {
        this(tag, ByteUtil.hexString2Bytes(value).length, value);
    }

    public BerTlv(String tag, int length, String value) {
        if (tag == null || value == null) {
            throw new IllegalArgumentException("BER-TLV tag and value cannot be null");
        }
        ByteUtil.hexString2Bytes(tag);
        int actualLength = ByteUtil.hexString2Bytes(value).length;
        if (length < 0 || length != actualLength) {
            throw new IllegalArgumentException("BER-TLV length does not match value length");
        }
        this.tag = tag;
        this.value = value;
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public String getTag() {
        return tag;
    }

    public String getValue() {
        return value;
    }

    /** 将当前元素恢复为 {@code Tag + Length + Value} 十六进制字符串。 */
    public String toHexString() {
        return BerTlvCodec.encodeHex(this);
    }

    /** 将当前元素恢复为 BER-TLV 字节数组。 */
    public byte[] toByteArray() {
        return BerTlvCodec.encode(this);
    }

}
