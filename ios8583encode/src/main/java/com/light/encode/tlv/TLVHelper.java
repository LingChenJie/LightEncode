package com.light.encode.tlv;

import android.util.Log;
import com.light.encode.Pair;
import com.light.encode.util.ByteUtil;
import com.light.encode.util.L;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("unused")
public final class TLVHelper {

    private TLVHelper() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static Map<String, TLV> builderMap(final byte[] bytes) {
        String hexString = ByteUtil.bytes2HexString(bytes);
        return builderMap(hexString);
    }

    public static Map<String, TLV> builderMap(String hexString) {
        if (hexString == null) {
            throw new IllegalArgumentException("TLV data cannot be null");
        }
        ByteUtil.hexString2Bytes(hexString); // 校验偶数长度及十六进制字符。
        hexString = hexString.toUpperCase(Locale.ROOT);
        int position = 0;
        LinkedHashMap<String, TLV> map = new LinkedHashMap<>();
        while (hexString.length() > position) {
            // get tag
            String tag = getTag(hexString, position);
            boolean bool = tag.trim().length() == 0 || tag.equals("00");
            if (bool) {
                break;
            }
            position += tag.length();
            // get length
            Pair<Integer, Integer> pair = getLength(hexString, position);
            int length = pair.first;
            position += pair.second;
            // get value
            int valueEnd = position + length * 2;
            if (valueEnd > hexString.length()) {
                throw new IllegalArgumentException("TLV value for tag " + tag + " is truncated");
            }
            String value = hexString.substring(position, valueEnd);
            position += value.length();
            // create TLV
            TLV tlv = new TLV(tag, length, value);
            map.put(tag, tlv);
            //LogUtils.d(L.TAG,"| " + tag + ": " + value);
        }
        if (L.PRINT_DEBUG_MSG) {
            Log.d(L.TAG,"===========================TLV-Decode-End===========================");
        }
        return map;
    }

    public static String tlv2HexString(final TLV tlv) {
        if (tlv == null || tlv.tag == null || tlv.value == null) {
            throw new IllegalArgumentException("TLV, tag and value cannot be null");
        }
        ByteUtil.hexString2Bytes(tlv.tag);
        int actualLength = ByteUtil.hexString2Bytes(tlv.value).length;
        if (actualLength != tlv.length) {
            throw new IllegalArgumentException("TLV length does not match value length");
        }
        StringBuilder builder = new StringBuilder();
        String length = length2HexString(tlv.length);
        builder.append(tlv.tag);
        builder.append(length);
        builder.append(tlv.value);
        return builder.toString();
    }

    public static byte[] tlv2Bytes(final TLV tlv) {
        String hexString = tlv2HexString(tlv);
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
    private static Pair<Integer, Integer> getLength(String hexString, int position) {
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
        return new Pair<>(length, size);
    }

    private static void requireAvailable(String hexString, int position, int required, String part) {
        if (position < 0 || required < 0 || position + required > hexString.length()) {
            throw new IllegalArgumentException("TLV " + part + " is truncated at character " + position);
        }
    }

    /**
     * 将TLV中数据长度转化成16进制字符串
     */
    public static String length2HexString(final int length) {
        if (length <= 0X7F) {
            return String.format("%02x", length);
        } else if (length <= 0XFF) {
            return "81" + String.format("%02x", length);
        } else if (length <= 0XFFFF) {
            return "82" + String.format("%04x", length);
        } else if (length <= 0XFFFFFF) {
            return "83" + String.format("%06x", length);
        } else {
            throw new RuntimeException("TLV length error");
        }
    }

}
