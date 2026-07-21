package com.light.encode.tlv;

import com.light.encode.util.ByteUtil;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** BER-TLV 编解码工具，支持 1～3 字节 Tag 和 definite-form Length。 */
@SuppressWarnings("unused")
public final class BerTlvCodec {

    private BerTlvCodec() {
        throw new AssertionError("No instances");
    }

    /** 将 BER-TLV 字节流按原顺序解析为 Map；重复 Tag 仅保留最后一个值。 */
    public static Map<String, BerTlv> decode(final byte[] bytes) {
        String hexString = ByteUtil.bytes2HexString(bytes);
        return decode(hexString);
    }

    /** 将不带空格的 BER-TLV 十六进制字符串解析为 Map。 */
    public static Map<String, BerTlv> decode(String hexString) {
        if (hexString == null) {
            throw new IllegalArgumentException("TLV data cannot be null");
        }
        ByteUtil.hexString2Bytes(hexString); // 校验偶数长度及十六进制字符。
        hexString = hexString.toUpperCase(Locale.ROOT);
        int position = 0;
        LinkedHashMap<String, BerTlv> map = new LinkedHashMap<>();
        while (hexString.length() > position) {
            // get tag
            String tag = getTag(hexString, position);
            boolean bool = tag.trim().length() == 0 || tag.equals("00");
            if (bool) {
                break;
            }
            position += tag.length();
            // get length
            LengthInfo lengthInfo = readLength(hexString, position);
            int length = lengthInfo.valueLength;
            position += lengthInfo.encodedChars;
            // get value
            int valueEnd = position + length * 2;
            if (valueEnd > hexString.length()) {
                throw new IllegalArgumentException("TLV value for tag " + tag + " is truncated");
            }
            String value = hexString.substring(position, valueEnd);
            position += value.length();
            // create TLV
            BerTlv tlv = new BerTlv(tag, length, value);
            map.put(tag, tlv);
        }
        return map;
    }

    /** 编码单个 TLV，并校验声明长度与 Value 实际字节数一致。 */
    public static String encodeHex(final BerTlv tlv) {
        if (tlv == null) {
            throw new IllegalArgumentException("TLV, tag and value cannot be null");
        }
        ByteUtil.hexString2Bytes(tlv.getTag());
        int actualLength = ByteUtil.hexString2Bytes(tlv.getValue()).length;
        if (actualLength != tlv.getLength()) {
            throw new IllegalArgumentException("TLV length does not match value length");
        }
        StringBuilder builder = new StringBuilder();
        String length = encodeLengthHex(tlv.getLength());
        builder.append(tlv.getTag());
        builder.append(length);
        builder.append(tlv.getValue());
        return builder.toString();
    }

    public static byte[] encode(final BerTlv tlv) {
        String hexString = encodeHex(tlv);
        return ByteUtil.hexString2Bytes(hexString);
    }

    /**
     * 取子域tag标签, tag标签不仅包含1个字节, 2个字节, 还包含3个字节
     */
    private static String getTag(String hexString, int position) {
        requireAvailable(hexString, position, 2, "tag");
        String tagString = hexString.substring(position, position + 2);
        int tagValue = Integer.parseInt(tagString, 16);
        if ((tagValue & 0x1F) != 0x1F) {
            return tagString;
        }
        int end = position + 2;
        for (int tagByteCount = 2; tagByteCount <= 3; tagByteCount++) {
            requireAvailable(hexString, end, 2, "tag");
            int value = Integer.parseInt(hexString.substring(end, end + 2), 16);
            end += 2;
            if ((value & 0x80) == 0) {
                return hexString.substring(position, end);
            }
        }
        throw new IllegalArgumentException("TLV tag exceeds 3 bytes");
    }

    /**
     * length域的编码比较简单, 最多有四个字节
     * 如果第一个字节的最高位b8为0, 则b7~b1的值就是value域的长度
     * 如果b8为1, b7~b1的值指示了下面有几个子字节, 下面子字节的值就是value域的长度
     */
    private static LengthInfo readLength(String hexString, int position) {
        requireAvailable(hexString, position, 2, "length");
        String lengthString = hexString.substring(position, position + 2);
        int lengthValue = Integer.parseInt(lengthString, 16);
        int size = 2;
        if ((lengthValue & 0x80) != 0) {
            int byteCount = lengthValue & 0x7F;
            if (byteCount == 0) {
                throw new IllegalArgumentException("Indefinite TLV length is not supported");
            }
            if (byteCount > 3) {
                throw new IllegalArgumentException("TLV length exceeds 3 bytes");
            }
            requireAvailable(hexString, position + size, byteCount * 2, "length");
            lengthString = hexString.substring(position + size, position + size + byteCount * 2);
            size += byteCount * 2;
        }
        int length = Integer.parseInt(lengthString, 16);
        return new LengthInfo(length, size);
    }

    private static void requireAvailable(String hexString, int position, int required, String part) {
        if (position < 0 || required < 0 || position + required > hexString.length()) {
            throw new IllegalArgumentException("TLV " + part + " is truncated at character " + position);
        }
    }

    /**
     * 将TLV中数据长度转化成16进制字符串
     */
    public static String encodeLengthHex(final int length) {
        if (length < 0) {
            throw new IllegalArgumentException("TLV length cannot be negative");
        }
        if (length <= 0X7F) {
            return String.format(Locale.ROOT, "%02X", length);
        } else if (length <= 0XFF) {
            return "81" + String.format(Locale.ROOT, "%02X", length);
        } else if (length <= 0XFFFF) {
            return "82" + String.format(Locale.ROOT, "%04X", length);
        } else if (length <= 0XFFFFFF) {
            return "83" + String.format(Locale.ROOT, "%06X", length);
        } else {
            throw new IllegalArgumentException("TLV length exceeds 0xFFFFFF");
        }
    }

    /** 长度值及其在十六进制输入中占用的字符数。 */
    private static final class LengthInfo {
        private final int valueLength;
        private final int encodedChars;

        private LengthInfo(int valueLength, int encodedChars) {
            this.valueLength = valueLength;
            this.encodedChars = encodedChars;
        }
    }

}
