package com.light.encode.tlv;

import com.light.encode.util.ByteUtil;

import java.io.Serializable;

@SuppressWarnings("unused")
public final class TLV implements Serializable {

    private static final long serialVersionUID = -5884698039382212843L;

    public int length;
    public String tag;
    public String value;

    public TLV(String tag, String value) {
        this.tag = tag;
        this.value = value;
        this.length = ByteUtil.hexString2Bytes(value).length;
    }

    public TLV(String tag, int length, String value) {
        this.tag = tag;
        this.value = value;
        this.length = length;
    }

    public String recover2HexString() {
        return TLVHelper.tlv2HexString(this);
    }

    public byte[] recover2Bytes() {
        return TLVHelper.tlv2Bytes(this);
    }

}