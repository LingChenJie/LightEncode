package com.light.encode.ios8583;

import android.util.Log;
import com.light.encode.util.ByteUtil;
import com.light.encode.util.RegularUtil;

import java.util.HashMap;
import java.util.Map;

/** ISO8583 解包实现；外部统一通过 {@link Iso8583Message#decode()} 调用。 */
final class Iso8583Decoder {

    /**
     * ISO8583 解码
     *
     * @param dataBytes      报文内容
     * @param lengthLength   报文长度所占长度
     * @param headerLength   报文头长度
     * @param fieldConfigMap 域配置map
     * @return
     */
    public static Iso8583Message decode(byte[] dataBytes, int lengthLength, int headerLength, Map<String, Iso8583Field> fieldConfigMap) {
        if (lengthLength < 0 || headerLength < 0) {
            throw new IllegalArgumentException("Message segment lengths cannot be negative");
        }
        if (fieldConfigMap == null) {
            throw new IllegalArgumentException("fieldConfigMap cannot be null");
        }
        requireAvailable(dataBytes, 0, lengthLength + headerLength + 8, "message header and bitmap");
        Iso8583Message.ResultBuilder builder = new Iso8583Message.ResultBuilder();

        log("----------------------------------------------------------------");
        log("-----------------ISO8583 decode start---------------------------");
        log("----------------------------------------------------------------");

        // length
        if (lengthLength > 0) {
            byte[] lengthBytes = new byte[lengthLength];
            System.arraycopy(dataBytes, 0, lengthBytes, 0, lengthLength);
            String lengthString = ByteUtil.bytes2HexString(lengthBytes);
            int length = Integer.parseInt(lengthString, 16);
            int actualLength = dataBytes.length - lengthLength;
            if (length != actualLength) {
                throw new IllegalArgumentException(
                        "Message length mismatch: declared " + length + ", actual " + actualLength);
            }
            log("| Length: " + lengthString + " (" + length + ")");
            builder.length(length);
        }

        // header
        if (headerLength > 0) {
            byte[] headerBytes = new byte[headerLength];
            System.arraycopy(dataBytes, lengthLength, headerBytes, 0, headerLength);
            String header = ByteUtil.bytes2HexString(headerBytes);
            log("| Header: " + header);
            builder.header(headerBytes);
        }

        // msgType
        int msgTypeLength = 0;
        String msgTypeFieldName = Iso8583FieldSupport.fieldName(Iso8583Constant.Position.MSG_TYPE);
        Iso8583Field msgTypeField = fieldConfigMap.get(msgTypeFieldName);
        if (msgTypeField != null) {
            int dataEncode = msgTypeField.dataEncodingCode();
            if (dataEncode == Iso8583FieldSupport.ENCODE_ASCII) {
                msgTypeLength = msgTypeField.getDataLength();
            } else {
                msgTypeLength = msgTypeField.getDataLength() / 2 + msgTypeField.getDataLength() % 2;
            }
            byte[] msgTypeBytes = new byte[msgTypeLength];
            requireAvailable(dataBytes, lengthLength + headerLength, msgTypeLength, "message type");
            System.arraycopy(dataBytes, lengthLength + headerLength, msgTypeBytes, 0, msgTypeLength);
            String msgType;
            if (dataEncode == Iso8583FieldSupport.ENCODE_ASCII) {
                msgType = ByteUtil.asciiBytes2String(msgTypeBytes);
            } else {
                msgType = ByteUtil.bytes2HexString(msgTypeBytes);
            }
            log("| MsgType: " + msgType);
            builder.messageType(msgType);
        }

        // bitmap
        int bitmapLength = 8;
        byte[] bitmapBytes = new byte[bitmapLength];
        System.arraycopy(dataBytes, lengthLength + headerLength + msgTypeLength, bitmapBytes, 0, bitmapLength);
        boolean[] bitmapBooleans = ByteUtil.bytes2BinaryBytes(bitmapBytes);
        if (bitmapBooleans[1]) { // 如果position为1, 则是可扩展位图
            bitmapLength = 16;
            requireAvailable(dataBytes, lengthLength + headerLength + msgTypeLength, bitmapLength,
                    "secondary bitmap");
            bitmapBytes = new byte[bitmapLength];
            System.arraycopy(dataBytes, lengthLength + headerLength + msgTypeLength, bitmapBytes, 0, bitmapLength);
            bitmapBooleans = ByteUtil.bytes2BinaryBytes(bitmapBytes);
        }
        String bitmapString = ByteUtil.bytes2HexString(bitmapBytes);
        log("| Bitmap: " + bitmapString);
        builder.bitmap(bitmapString);

        // body - all field
        HashMap<String, Iso8583Field> fieldMap = new HashMap<>();
        int index = lengthLength + headerLength + msgTypeLength + bitmapLength;
        for (int position = 2; position < bitmapBooleans.length; position++) {
            boolean bool = bitmapBooleans[position];
            if (!bool) {// 该域无值，继续循环下一次
                continue;
            }
            String fieldName = Iso8583FieldSupport.fieldName(position);
            Iso8583Field field = fieldConfigMap.get(fieldName);
            if (field == null) {
                // 未知域没有长度信息，无法可靠定位后续域，继续会造成静默错位。
                throw new IllegalArgumentException("Iso8583Field [" + position + "] is present but not configured");
            }
            int fieldAlignType = field.alignCode();
            int fieldDataEncode = field.dataEncodingCode();
            int fieldDataLength = field.getDataLength();
            int fieldLengthType = field.lengthTypeCode();
            int fieldLengthEncode = field.lengthEncodingCode();
            String desc = field.getDesc();
            // handle length
            int bytesDataLength = 0;
            String dataLengthString;// field data length
            if (fieldLengthType > Iso8583FieldSupport.LENGTH_VAR_NONE) {// 变长
                // handle byte array of length
                int variableLength;
                if (fieldLengthEncode == Iso8583FieldSupport.ENCODE_BCD) {
                    variableLength = (fieldLengthType + 1) / 2;
                    requireAvailable(dataBytes, index, variableLength, "length of field " + position);
                    byte[] variableLengthBytes = new byte[variableLength];
                    System.arraycopy(dataBytes, index, variableLengthBytes, 0, variableLength);
                    fieldDataLength = ByteUtil.bcd2Int(variableLengthBytes);// BCD  0120 -> 120
                    dataLengthString = String.format("%0" + fieldLengthType + "d", fieldDataLength);
                    if (dataLengthString.length() % 2 != 0) {
                        dataLengthString = "0" + dataLengthString;// 长度前补零
                    }
                } else if (fieldLengthEncode == Iso8583FieldSupport.ENCODE_ASCII) {
                    variableLength = fieldLengthType;
                    requireAvailable(dataBytes, index, variableLength, "length of field " + position);
                    byte[] variableLengthBytes = new byte[variableLength];
                    System.arraycopy(dataBytes, index, variableLengthBytes, 0, variableLength);
                    dataLengthString = ByteUtil.asciiBytes2String(variableLengthBytes);
                    try {
                        fieldDataLength = Integer.parseInt(dataLengthString);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Iso8583Field [" + position + "] has an invalid ASCII length", e);
                    }
                } else {
                    throw new RuntimeException("fieldLengthEncode not support");
                }
                if (fieldDataEncode == Iso8583FieldSupport.ENCODE_BCD) {
                    bytesDataLength = fieldDataLength / 2 + fieldDataLength % 2;
                } else if (fieldDataEncode == Iso8583FieldSupport.ENCODE_BIT) {
                    bytesDataLength = fieldDataLength;
                } else if (fieldDataEncode == Iso8583FieldSupport.ENCODE_ASCII) {
                    bytesDataLength = fieldDataLength;
                }
                index = index + variableLength;
            } else {// 定长
                if (fieldDataEncode == Iso8583FieldSupport.ENCODE_BCD) {
                    bytesDataLength = fieldDataLength / 2 + fieldDataLength % 2;
                } else if (fieldDataEncode == Iso8583FieldSupport.ENCODE_BIT) {
                    bytesDataLength = fieldDataLength;
                } else if (fieldDataEncode == Iso8583FieldSupport.ENCODE_ASCII) {
                    bytesDataLength = fieldDataLength;
                }
                dataLengthString = String.valueOf(fieldDataLength);
            }
            requireAvailable(dataBytes, index, bytesDataLength, "value of field " + position);
            if (Iso8583Log.ENABLED) {
                log("position:" + position + " dataLength:" + dataLengthString);
            }
            byte[] fieldDataBytes = new byte[bytesDataLength];
            System.arraycopy(dataBytes, index, fieldDataBytes, 0, bytesDataLength);
            index = index + bytesDataLength;
            // handle data
            String dataString = ByteUtil.bytes2HexString(fieldDataBytes);
            if (fieldDataEncode == Iso8583FieldSupport.ENCODE_ASCII) {
                String gbkString = ByteUtil.hexString2GBKString(dataString);
                boolean isContainZH = RegularUtil.isContainChinese(gbkString);
                if (isContainZH) {
                    dataString = gbkString;
                } else {
                    dataString = ByteUtil.asciiBytes2String(fieldDataBytes);
                }
            }
            field.setDataString(dataString);
            field.setDataBytes(fieldDataBytes);
            // 压缩 BCD 的奇数逻辑长度会占用完整字节，需要按对齐方式去掉补入的半字节。
            // 该规则同时适用于定长 N3（如 DE22/DE23）和变长奇数位字段（如 PAN）。
            if (fieldDataEncode == Iso8583FieldSupport.ENCODE_BCD
                    && dataString.length() > fieldDataLength) {
                if (fieldAlignType == Iso8583FieldSupport.ALIGN_LEFT) {
                    dataString = dataString.substring(0, fieldDataLength);
                } else {
                    int length = dataString.length();
                    dataString = dataString.substring(length - fieldDataLength, length);
                }
                field.setDataString(dataString);
            } else if (fieldLengthType > Iso8583FieldSupport.LENGTH_VAR_NONE
                    && fieldDataEncode == Iso8583FieldSupport.ENCODE_BIT) {
                    if (fieldAlignType == Iso8583FieldSupport.ALIGN_LEFT) {
                        dataString = dataString.substring(0, fieldDataLength * 2);
                    } else {
                        int length = dataString.length();
                        dataString = dataString.substring(length - fieldDataLength * 2, length);
                    }
                field.setDataString(dataString);
            }
            if (fieldLengthType > Iso8583FieldSupport.LENGTH_VAR_NONE) {
                field.setDataLength(fieldDataLength);
            }
            fieldMap.put(fieldName, field);
            log("| [" + fieldName + "]: [" + dataLengthString + "] " + dataString + "      [" + desc + "]");
        }
        builder.fields(fieldMap);

        byte[] allFieldDataBytes = new byte[dataBytes.length - lengthLength - headerLength - msgTypeLength];
        System.arraycopy(dataBytes, lengthLength + headerLength + msgTypeLength, allFieldDataBytes, 0, allFieldDataBytes.length);
        builder.allFieldData(allFieldDataBytes);

        log("----------------------------------------------------------------");
        log("-----------------ISO8583 decode end-----------------------------");
        log("----------------------------------------------------------------");
        return builder.build();
    }

    private static void requireAvailable(byte[] data, int offset, int length, String part) {
        if (data == null || offset < 0 || length < 0 || offset > data.length - length) {
            throw new IllegalArgumentException("Insufficient data for " + part);
        }
    }

    /** Detailed messages may contain card data, so logging is opt-in. */
    private static void log(String message) {
        if (Iso8583Log.ENABLED) {
            Log.d(Iso8583Log.TAG, message);
        }
    }

}
