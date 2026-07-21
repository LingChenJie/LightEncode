package com.light.encode.ios8583;

import com.light.encode.util.ByteUtil;

import org.junit.Test;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static com.light.encode.ios8583.Iso8583Constant.AlignType.LEFT;
import static com.light.encode.ios8583.Iso8583Constant.AlignType.RIGHT;
import static com.light.encode.ios8583.Iso8583Constant.EncodeType.ASCII;
import static com.light.encode.ios8583.Iso8583Constant.EncodeType.BCD;
import static com.light.encode.ios8583.Iso8583Constant.EncodeType.BIT;
import static com.light.encode.ios8583.Iso8583Constant.LengthType.LLVAR;
import static com.light.encode.ios8583.Iso8583Constant.LengthType.NONE;

public class Iso8583CodecTest {

    @Test
    public void encodeAndDecodePreserveFieldLengthSemantics() {
        Iso8583Config.load(Arrays.asList(
                field(0, BCD, NONE, BCD, 4),
                field(2, BCD, LLVAR, BCD, 19),
                field(3, BCD, NONE, ASCII, 6),
                field(52, BCD, NONE, BIT, 8),
                field(70, ASCII, LLVAR, ASCII, 99)
        ));

        byte[] encoded = new Iso8583Message.EncodeBuilder()
                .messageType("0800")
                .field(2, "6228888888812127121")
                .field(3, "ABC")
                .field(52, "439139CC5AEF058B")
                .field(70, "XYZ")
                .lengthHeaderBytes(0)
                .build()
                .encode();

        // MTI(2) + secondary bitmap(16) 后是 F002 的 BCD LLVAR 长度：19 位数字。
        assertEquals(0x19, encoded[18] & 0xFF);
        assertTrue((encoded[2] & 0x80) != 0);

        Iso8583Message decoded = new Iso8583Message.DecodeBuilder()
                .lengthHeaderBytes(0)
                .data(encoded)
                .build()
                .decode();

        assertEquals("0800", decoded.getMessageType());
        assertEquals("6228888888812127121", decoded.getFieldMap().get("F002").getDataString());
        assertEquals(10, decoded.getFieldMap().get("F002").getDataBytes().length);
        assertEquals(6, decoded.getFieldMap().get("F003").getDataLength());
        assertEquals("ABC000", decoded.getFieldMap().get("F003").getDataString());
        assertEquals("439139CC5AEF058B", decoded.getFieldMap().get("F052").getDataString());
        assertEquals("XYZ", decoded.getFieldMap().get("F070").getDataString());
        assertArrayEquals(ByteUtil.hexString2Bytes("439139CC5AEF058B"),
                decoded.getFieldMap().get("F052").getDataBytes());
    }

    @Test
    public void decodeRejectsIncorrectMessageLengthHeader() {
        Iso8583Config.load(Arrays.asList(
                field(0, BCD, NONE, BCD, 4),
                field(3, BCD, NONE, BCD, 6)
        ));
        byte[] encoded = new Iso8583Message.EncodeBuilder()
                .messageType("0200")
                .field(3, "000000")
                .build()
                .encode();
        encoded[1]--;

        assertThrows(IllegalArgumentException.class, () -> new Iso8583Message.DecodeBuilder()
                .data(encoded)
                .build()
                .decode());
    }

    @Test
    public void oddFixedBcdFieldRemovesAlignmentNibbleWhenDecoded() {
        Iso8583Config.load(Arrays.asList(
                field(0, BCD, NONE, BCD, 4),
                new Iso8583Field.Builder()
                        .position(22)
                        .lengthEncode(BCD)
                        .lengthType(NONE)
                        .dataEncode(BCD)
                        .dataLength(3)
                        .alignType(RIGHT)
                        .padding("0")
                        .build()
        ));

        byte[] encoded = new Iso8583Message.EncodeBuilder()
                .messageType("0200")
                .field(22, "051")
                .lengthHeaderBytes(0)
                .build()
                .encode();
        Iso8583Message decoded = new Iso8583Message.DecodeBuilder()
                .data(encoded)
                .lengthHeaderBytes(0)
                .build()
                .decode();

        assertEquals("051", decoded.getFieldMap().get("F022").getDataString());
        assertEquals("0051", ByteUtil.bytes2HexString(
                decoded.getFieldMap().get("F022").getDataBytes()));
    }

    @Test
    public void configurationRejectsDuplicatePositions() {
        assertThrows(IllegalArgumentException.class, () -> Iso8583Config.load(Arrays.asList(
                field(0, BCD, NONE, BCD, 4),
                field(0, ASCII, NONE, ASCII, 4)
        )));
    }

    @Test
    public void fieldKeysDoNotDependOnDeviceLocale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("ar"));
            assertEquals("F002", Iso8583FieldSupport.fieldName(2));
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    public void decodeDoesNotMutateCallerConfiguration() {
        Iso8583Config.load(Arrays.asList(
                field(0, BCD, NONE, BCD, 4),
                field(3, BCD, NONE, BCD, 6)
        ));
        byte[] encoded = new Iso8583Message.EncodeBuilder()
                .messageType("0200")
                .field(3, "000000")
                .lengthHeaderBytes(0)
                .build()
                .encode();
        Map<String, Iso8583Field> config = Iso8583Config.fieldConfigSnapshot();

        new Iso8583Message.DecodeBuilder()
                .data(encoded)
                .lengthHeaderBytes(0)
                .build()
                .decode(config);

        assertNull(config.get("F003").getDataString());
    }

    @Test
    public void publicFieldTypesAndConfigurationSnapshotsAreStable() {
        Iso8583Field configured = field(3, BCD, NONE, ASCII, 6);
        Iso8583Config.load(Arrays.asList(field(0, BCD, NONE, BCD, 4), configured));

        assertEquals(LEFT, configured.getAlignType());
        assertEquals(NONE, configured.getLengthType());
        assertEquals(BCD, configured.getLengthEncode());
        assertEquals(ASCII, configured.getDataEncode());

        Map<String, Iso8583Field> templates = Iso8583Config.fieldTemplates();
        templates.get("F003").setDataLength(99);
        assertEquals(6, Iso8583Config.fieldConfigSnapshot().get("F003").getDataLength());
        assertThrows(UnsupportedOperationException.class,
                () -> templates.put("F004", field(4, BCD, NONE, BCD, 12)));
    }

    @Test
    public void messageDefensivelyCopiesByteArrays() {
        Iso8583Config.load(Arrays.asList(field(0, BCD, NONE, BCD, 4)));
        byte[] header = ByteUtil.hexString2Bytes("6000010000");
        Iso8583Message message = new Iso8583Message.EncodeBuilder()
                .header(header)
                .messageType("0800")
                .build();

        header[0] = 0;
        byte[] returnedHeader = message.getHeader();
        returnedHeader[0] = 0;
        assertEquals("6000010000", ByteUtil.bytes2HexString(message.getHeader()));
    }

    private static Iso8583Field field(int position, String lengthEncode, String lengthType,
                               String dataEncode, int dataLength) {
        return new Iso8583Field.Builder()
                .position(position)
                .lengthEncode(lengthEncode)
                .lengthType(lengthType)
                .dataEncode(dataEncode)
                .dataLength(dataLength)
                .alignType(LEFT)
                .padding("0")
                .build();
    }
}
