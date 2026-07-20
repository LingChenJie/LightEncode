package com.light.encode.ios8583;

import com.light.encode.util.ByteUtil;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Iso8583CodecTest {

    @Test
    public void encodeAndDecodePreserveFieldLengthSemantics() {
        Iso8583Config.setBitmapConfig(Arrays.asList(
                field(0, "BCD", "NONE", "BCD", 4),
                field(2, "BCD", "PAIR", "BCD", 19),
                field(3, "BCD", "NONE", "ASC", 6),
                field(52, "BCD", "NONE", "BIT", 8),
                field(70, "ASC", "PAIR", "ASC", 99)
        ));

        byte[] encoded = new Iso8583.EncodeBuilder()
                .addMsgType("0800")
                .addField(2, "6228888888812127121")
                .addField(3, "ABC")
                .addField(52, "439139CC5AEF058B")
                .addField(70, "XYZ")
                .addLengthLength(0)
                .build()
                .encode();

        // MTI(2) + secondary bitmap(16) 后是 F002 的 BCD LLVAR 长度：19 位数字。
        assertEquals(0x19, encoded[18] & 0xFF);
        assertTrue((encoded[2] & 0x80) != 0);

        Iso8583 decoded = new Iso8583.DecodeBuilder()
                .addLengthLength(0)
                .addDataBytes(encoded)
                .build()
                .decode();

        assertEquals("0800", decoded.getMsgType());
        assertEquals("6228888888812127121", decoded.getFieldMap().get("F002").getDataString());
        assertEquals(10, decoded.getFieldMap().get("F002").getDataBytes().length);
        assertEquals(6, decoded.getFieldMap().get("F003").getDataLength());
        assertEquals("439139CC5AEF058B", decoded.getFieldMap().get("F052").getDataString());
        assertEquals("XYZ", decoded.getFieldMap().get("F070").getDataString());
        assertArrayEquals(ByteUtil.hexString2Bytes("439139CC5AEF058B"),
                decoded.getFieldMap().get("F052").getDataBytes());
    }

    private static Field field(int position, String lengthEncode, String lengthType,
                               String dataEncode, int dataLength) {
        return new Field.Builder()
                .position(position)
                .lengthEncode(lengthEncode)
                .lengthType(lengthType)
                .dataEncode(dataEncode)
                .dataLength(dataLength)
                .alignType("LEFT")
                .padding("0")
                .build();
    }
}
