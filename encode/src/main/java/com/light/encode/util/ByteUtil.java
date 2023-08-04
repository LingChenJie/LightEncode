package com.light.encode.util;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

@SuppressWarnings({"CharsetObjectCanBeUsed", "unused", "StringConcatenationInLoop"})
public final class ByteUtil {

    private ByteUtil() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 字节数组转换为16进制字符串
     */
    public static String bytes2HexString(final byte[] bytes) {
        byte[] buffer = new byte[bytes.length * 2];
        for (int i = 0, j = 0; i < bytes.length; i++) {
            byte value = bytes[i];
            buffer[j++] = (byte) int2HexChar(value >> 4 & 0X0F);
            buffer[j++] = (byte) int2HexChar(value & 0X0F);
        }
        return new String(buffer);
    }

    /**
     * 16进制字符串转换为字节数组
     */
    public static byte[] hexString2Bytes(final String hexString) {
        if (hexString.length() % 2 != 0) {
            throw new RuntimeException("The hexString length has to be a multiple of 2.");
        }
        int length = hexString.length() / 2;
        byte[] buffer = new byte[length];
        char[] charArray = hexString.toCharArray();
        for (int i = 0; i < length; i++) {
            byte high = (byte) charArray[i * 2];
            byte low = (byte) charArray[i * 2 + 1];
            int value = hexChar2Int(high) << 4 | hexChar2Int(low);
            buffer[i] = (byte) (value);
        }
        return buffer;
    }

    public static String bcdBytes2String(final byte[] bytes) {
        int length = bytes.length;
        char[] buffer = new char[length * 2];
        int index = 0;
        for (byte value : bytes) {
            int modulusH = value & 0X00F0;
            boolean b1 = (modulusH >> 4) > 9;
            int highH = (modulusH >> 4) + 55;
            int highL = (modulusH >> 4) + 48;
            buffer[index++] = (char) (b1 ? highH : highL);
            int modulusL = value & 0X000F;
            boolean b2 = modulusL > 9;
            int lowH = modulusL + 55;
            int lowL = modulusL + 48;
            buffer[index++] = (char) (b2 ? lowH : lowL);
        }
        return new String(buffer);
    }

    public static byte[] string2BcdBytes(final String string) {
        if (string.length() % 2 != 0) {
            throw new RuntimeException("The string length has to be a multiple of 2.");
        }
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            char[] charArray = string.toCharArray();
            byteArrayOutputStream = new ByteArrayOutputStream();
            for (int i = 0; i < charArray.length; i += 2) {
                int high = charArray[i] - 48;
                int low = charArray[i + 1] - 48;
                byteArrayOutputStream.write(high << 4 | low);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * ASCII字节数组转换为BCD字节数组
     */
    public static byte[] asciiBytes2BcdBytes(final byte[] bytes) {
        byte[] buffer = new byte[bytes.length / 2];
        for (int i = 0; i < buffer.length; i++) {
            byte high = bytes[i * 2];
            byte low = bytes[i * 2 + 1];
            int value = hexChar2Int(high) << 4 | hexChar2Int(low);
            buffer[i] = (byte) (value);
        }
        return buffer;
    }

    /**
     * BCD字节数据转换为ASCII字节数组
     */
    public static byte[] bcdBytes2AsciiBytes(final byte[] bytes) {
        byte[] buffer = new byte[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            buffer[i * 2] = (byte) int2HexChar(bytes[i] >> 4 & 0X0F);
            buffer[i * 2 + 1] = (byte) int2HexChar(bytes[i] & 0X0F);
        }
        return buffer;
    }

    /**
     * 二进制数组转换为字节数组
     */
    public static byte[] binaryBytes2Bytes(final boolean[] booleans) {
        int len = booleans.length - 1;
        int size = len / 8;
        int remainder = len % 8;
        if (remainder != 0) {
            size = size + 1;
        }
        StringBuilder string = new StringBuilder();
        byte[] buffer = new byte[size];
        for (int i = 1; i < booleans.length; i++) {
            boolean bool = booleans[i];
            if (bool) {
                string.append("1");
            } else {
                string.append("0");
            }
        }
        int j = 0;
        String temp;
        for (int i = 0; i < string.length(); i = i + 8) {
            temp = string.substring(i, i + 8);
            buffer[j] = binaryString2Byte(temp);
            j = j + 1;
        }
        return buffer;
    }

    /**
     * 字节数组转换为二进制数组
     */
    public static boolean[] bytes2BinaryBytes(final byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        boolean[] booleans = new boolean[bytes.length * 8 + 1];
        for (byte value : bytes) {
            builder.append(getBinaryLengthStringByByte(value));
        }
        String string = builder.toString();
        for (int i = 0; i < string.length(); i++) {
            boolean bool = string.substring(i, i + 1).equalsIgnoreCase("1");
            booleans[i + 1] = bool;
        }
        return booleans;
    }

    public static byte[] string2Bytes(final String string, final String charsetName) {
        try {
            Charset charset = Charset.forName(charsetName);
            return string.getBytes(charset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] asciiString2Bytes(final String string) {
        try {
            Charset charset = Charset.forName("US-ASCII");
            return string.getBytes(charset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String asciiBytes2String(final byte[] bytes) {
        try {
            Charset charset = Charset.forName("US-ASCII");
            return new String(bytes, charset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 十六进制字符串转换为二进制字符串
     */
    public static String hexStringToBinaryString(final String hexString) {
        if (hexString.length() == 0) return "";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < hexString.length(); i++) {
            String indexString = hexString.substring(i, i + 1);
            int value = Integer.parseInt(indexString, 16);
            String binaryString = Integer.toBinaryString(value);
            while (binaryString.length() < 4) {
                binaryString = "0" + binaryString;
            }
            builder.append(binaryString);
        }
        return builder.toString();
    }

    public static String hexString2String(final String hexString) {
        try {
            byte[] bytes = hexString2Bytes(hexString);
            Charset charset = Charset.forName("UTF-8");
            return new String(bytes, charset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String hexString2AsciiString(final String hexString) {
        try {
            byte[] bytes = hexString2Bytes(hexString);
            Charset charset = Charset.forName("US-ASCII");
            return new String(bytes, charset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String hexString2GBKString(final String hexString) {
        try {
            Charset charset = Charset.forName("GBK");
            byte[] bytes = hexString2Bytes(hexString);
            return new String(bytes, charset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String string2HexString(final String string) {
        try {
            Charset charset = Charset.forName("US-ASCII");
            byte[] bytes = string.getBytes(charset);
            return bytes2HexString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getBinaryLengthStringByByte(int value) {
        // if this is a positive number its bits number will be less than 8
        // so we have to fill it to be a 8 digit binary string b = b + 100000000(2^8 = 256) then only get the lower 8 digit
        value |= 256; // mark the 9th digit as 1 to make sure the string
        // has at least 8 digits
        String string = Integer.toBinaryString(value);
        int len = string.length();
        return string.substring(len - 8, len);
    }

    public static byte hexString2Byte(final String hexString) {
        return (byte) Integer.parseInt(hexString, 16);
    }

    /**
     * 二进制字符串转换为单个字节
     */
    public static byte binaryString2Byte(String string) {
        byte value;
        boolean bool = string.substring(0, 1).equalsIgnoreCase("1");
        if (bool) {
            // get lower 7 digits original code
            string = "0" + string.substring(1);
            value = Byte.valueOf(string, 2);
            // then recover the 8th digit as 1 equal to plus 1000000
            value |= 128;
        } else {
            value = Byte.valueOf(string, 2);
        }
        return value;
    }

    /**
     * Int值转换为16进制字符
     */
    public static char int2HexChar(int value) {
        if (value > 9) {
            return (char) (value - 10 + 'A');
        }
        return (char) (value + '0');
    }

    /**
     * 16进制字符转换为Int值
     */
    public static int hexChar2Int(byte value) {
        if (value >= 'a') {
            return (value - 'a' + 10) & 0X0F;
        }
        if (value >= 'A') {
            return (value - 'A' + 10) & 0X0F;
        }
        return (value - '0') & 0X0F;
    }

    /**
     * 将BCD码转成Int
     */
    public static int bcd2Int(byte[] b) {
        String string = "";
        for (byte value : b) {
            int high = (value & 0xF0) >> 4;
            int low = value & 0x0F;
            string += (char) (high + 48);
            string += (char) (low + 48);
        }
        return Integer.parseInt(string);
    }

}