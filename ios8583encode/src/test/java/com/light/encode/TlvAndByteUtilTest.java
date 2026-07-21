package com.light.encode;

import com.light.encode.tlv.BerTlv;
import com.light.encode.tlv.BerTlvCodec;
import com.light.encode.util.ByteUtil;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class TlvAndByteUtilTest {

    @Test
    public void tlvRoundTripSupportsOneAndTwoByteTags() {
        String encoded = "5A0212349F3303E0F8C8";
        Map<String, BerTlv> values = BerTlvCodec.decode(encoded);

        assertEquals("1234", values.get("5A").getValue());
        assertEquals("E0F8C8", values.get("9F33").getValue());
        assertEquals("9F3303E0F8C8", values.get("9F33").toHexString());
    }

    @Test
    public void malformedInputsFailWithClearExceptions() {
        assertThrows(IllegalArgumentException.class, () -> BerTlvCodec.decode("5A02FF"));
        assertThrows(IllegalArgumentException.class, () -> BerTlvCodec.decode("9F"));
        assertThrows(IllegalArgumentException.class, () -> ByteUtil.hexString2Bytes("0G"));
        assertThrows(IllegalArgumentException.class, () -> BerTlvCodec.encodeLengthHex(-1));
        assertThrows(IllegalArgumentException.class, () -> new BerTlv("5A", 2, "12"));
    }
}
