package com.light.encode.tlv;

import com.android.architecture.utils.LogUtils;
import com.light.encode.Pair;
import com.light.encode.util.ByteUtil;
import com.light.encode.util.L;

import java.util.HashMap;
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
        Locale local = Locale.getDefault();
        hexString = hexString.toUpperCase(local);
        int position = 0;
        HashMap<String, TLV> map = new HashMap<>();
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
            String value = hexString.substring(position, position + length * 2);
            position += value.length();
            // create TLV
            TLV tlv = new TLV(tag, length, value);
            map.put(tag, tlv);
            //LogUtils.d(L.TAG,"| " + tag + ": " + value);
        }
        LogUtils.d(L.TAG,"===========================TLV-Decode-End===========================");
        return map;
    }

    public static String tlv2HexString(final TLV tlv) {
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
        String tag;
        String tagString = hexString.substring(position, position + 2);
        int tagValue = Integer.parseInt(tagString, 16);

        String tag2String = hexString.substring(position + 2, position + 4);
        int tag2Value = Integer.parseInt(tag2String, 16);

        // b5~b1 如果全为1, 则说明这个tag下面还有一个子字节, emv里的tag最多占两个字节
        int temp = tagValue & 0x1F;
        if (temp == 0x1F) {
            temp = tag2Value & 0x80;
            if (temp == 0x80) { // 除tag标签首字节外, tag中其他字节最高位为: 1 表示后续还有字节, 0 表示为最后一个字节
                tag = hexString.substring(position, position + 6); // 3 bytes的tag
            } else {
                tag = hexString.substring(position, position + 4); // 2 bytes的tag
            }
        } else {
            tag = hexString.substring(position, position + 2);
        }
        return tag;
    }

    /**
     * length域的编码比较简单, 最多有四个字节
     * 如果第一个字节的最高位b8为0, 则b7~b1的值就是value域的长度
     * 如果b8为1, b7~b1的值指示了下面有几个子字节, 下面子字节的值就是value域的长度
     */
    private static Pair<Integer, Integer> getLength(String hexString, int position) {
        String lengthString = hexString.substring(position, position + 2);
        int lengthValue = Integer.parseInt(lengthString, 16);
        int temp = lengthValue & 0x80;
        int size = 2;
        if (temp != 0) {
            int value = lengthValue & 0x7F; // 127 的二进制 0111 1111
            lengthString = hexString.substring(position + size, position + size + value * 2);
            size += value * 2;
        }
        int length = Integer.parseInt(lengthString, 16);
        return new Pair<>(length, size);
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