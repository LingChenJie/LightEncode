package com.light.encode;

import com.light.encode.tlv.TLV;
import com.light.encode.tlv.TLVHelper;
import com.light.encode.util.ByteUtil;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class TlvAndByteUtilTest {

    @Test
    public void tlvRoundTripSupportsOneAndTwoByteTags() {
        String encoded = "5A0212349F3303E0F8C8";
        Map<String, TLV> values = TLVHelper.builderMap(encoded);

        assertEquals("1234", values.get("5A").value);
        assertEquals("E0F8C8", values.get("9F33").value);
        assertEquals("9F3303E0F8C8", values.get("9F33").recover2HexString());
    }

    @Test
    public void malformedInputsFailWithClearExceptions() {
        assertThrows(IllegalArgumentException.class, () -> TLVHelper.builderMap("5A02FF"));
        assertThrows(IllegalArgumentException.class, () -> TLVHelper.builderMap("9F"));
        assertThrows(IllegalArgumentException.class, () -> ByteUtil.hexString2Bytes("0G"));
    }
}
