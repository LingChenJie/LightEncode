package com.light.encode.ios8583;

import android.util.Log;
import com.light.encode.util.ByteUtil;

import java.util.Iterator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** ISO8583 组包实现；外部统一通过 {@link Iso8583Message#encode()} 调用。 */
final class Iso8583Encoder {


    public static byte[] encode(Map<String, Iso8583Field> map, int lengthLength, byte[] header, String msgType, boolean hasBitmap) {
        if (map == null || map.isEmpty()) {
            throw new IllegalArgumentException("ISO8583 fields cannot be empty");
        }
        if (lengthLength < 0) {
            throw new IllegalArgumentException("Message length header cannot be negative");
        }
        TreeMap<String, Iso8583Field> fieldMap = new TreeMap<>(map);
        validateFieldPositions(fieldMap);

        log("----------------------------------------------------------------");
        log("-----------------ISO8583 encode start---------------------------");
        log("----------------------------------------------------------------");

        // header
        int headerLength = 0;
        if (header != null && header.length > 0) {
            headerLength = header.length;
            String hexString = ByteUtil.bytes2HexString(header);
            log("| Header: " + hexString);
        }

        // msg type
        int msgTypeLength = 0;
        byte[] msgTypeBytes = new byte[0];

        // calculate all field data and data length
        int bodyLength = 0;
        boolean has128Bitmap = false;
        Iterator<Map.Entry<String, Iso8583Field>> iterator = fieldMap.entrySet().iterator();
        boolean next = iterator.hasNext();
        while (next) {
            Map.Entry<String, Iso8583Field> entry = iterator.next();
            Iso8583Field field = entry.getValue();
            int fieldPosition = field.getPosition();
            int fieldDataEncode = field.dataEncodingCode();
            int fieldLengthType = field.lengthTypeCode();
            int fieldLengthEncode = field.lengthEncodingCode();
            int fieldDataLength = field.getDataLength();

            // msg type
            if (fieldPosition == Iso8583Constant.Position.MSG_TYPE) {
                if (msgType != null && !msgType.isEmpty()) {
                    if (fieldDataEncode == Iso8583FieldSupport.ENCODE_ASCII) {
                        String msgTypeHex = ByteUtil.string2HexString(msgType);
                        msgTypeBytes = ByteUtil.hexString2Bytes(msgTypeHex);
                        log("| MsgType: " + msgTypeHex + " (" + msgType + ")");
                    } else {
                        msgTypeBytes = ByteUtil.hexString2Bytes(msgType);
                        log("| MsgType: " + msgType);
                    }
                    msgTypeLength = msgTypeBytes.length;
                }
            }
            // ignore msg type field and bitmap field
            if (fieldPosition == Iso8583Constant.Position.MSG_TYPE || fieldPosition == Iso8583Constant.Position.BITMAP) {
                next = iterator.hasNext();
                continue;
            }
            // has 128 bitmap
            if (fieldPosition > 64) {
                has128Bitmap = true;
            }
            // calculate the total length of the request data
            int bytesDataLength = encodedDataLength(fieldDataEncode, fieldDataLength);
            if (fieldLengthType > Iso8583FieldSupport.LENGTH_VAR_NONE) {// 变长
                switch (fieldLengthEncode) {
                    case Iso8583FieldSupport.ENCODE_BCD:
                        bytesDataLength += (fieldLengthType + 1) / 2;
                        break;
                    case Iso8583FieldSupport.ENCODE_ASCII:
                        bytesDataLength += fieldLengthType;
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported length encoding for field " + fieldPosition);
                }
            }
            if (Iso8583Log.ENABLED) {
                log("fieldPosition: " + fieldPosition + " bytesDataLength:" + bytesDataLength);
            }
            bodyLength += bytesDataLength;
            next = iterator.hasNext();
        }

        // total length
        int bitmapLength = 0;
        if (hasBitmap) {
            if (has128Bitmap) {
                bitmapLength = 16;
                String name = Iso8583FieldSupport.fieldName(1);
                Iso8583Field field = Iso8583FieldSupport.emptyField(1);
                fieldMap.put(name, field);
            } else {
                bitmapLength = 8;
            }
        }
        int totalLength = lengthLength + headerLength + msgTypeLength + bitmapLength + bodyLength;
        if (Iso8583Log.ENABLED) {
            log("totalLength: " + totalLength + " lengthLength: " + lengthLength + " headerLength: " + headerLength + " msgTypeLength: " + msgTypeLength + " bitmapLength: " + bitmapLength + " bodyLength: " + bodyLength);
        }
        byte[] content = new byte[totalLength];
        if (headerLength > 0) {
            System.arraycopy(header, 0, content, lengthLength, headerLength);
        }
        if (msgTypeLength > 0) {
            System.arraycopy(msgTypeBytes, 0, content, lengthLength + headerLength, msgTypeLength);
        }

        // body - all field
        int index = lengthLength + headerLength + msgTypeLength + bitmapLength;
        boolean[] bitmapBinaryBytes = new boolean[bitmapLength * 8 + 1];
        Iterator<Map.Entry<String, Iso8583Field>> entryIterator = fieldMap.entrySet().iterator();
        next = entryIterator.hasNext();
        while (next) {
            if (Iso8583Log.ENABLED) {
                log("before index:" + index);
            }
            Map.Entry<String, Iso8583Field> entry = entryIterator.next();
            Iso8583Field field = entry.getValue();
            int fieldPosition = field.getPosition();
            String fieldName = Iso8583FieldSupport.fieldName(fieldPosition);
            String fieldPadding = field.getPadding();
            int fieldAlignType = field.alignCode();
            int fieldDataEncode = field.dataEncodingCode();
            int fieldLengthType = field.lengthTypeCode();
            int fieldLengthEncode = field.lengthEncodingCode();
            int fieldDataLength = field.getDataLength();
            byte[] fieldDataBytes = field.getDataBytes();
            String fieldDataString = field.getDataString();
            String desc = field.getDesc();
            // mark each field
            if (hasBitmap) {
                bitmapBinaryBytes[fieldPosition] = true;
            }
            // ignore msg type field and bitmap field
            if (fieldPosition == Iso8583Constant.Position.MSG_TYPE || fieldPosition == Iso8583Constant.Position.BITMAP) {
                next = entryIterator.hasNext();
                continue;
            }
            // calculate the length of variable length data
            String variableLengthString = "";
            if (fieldLengthType > Iso8583FieldSupport.LENGTH_VAR_NONE) {
                int maxLength = fieldLengthType == Iso8583FieldSupport.LENGTH_LLVAR ? 99 : 999;
                if (fieldDataLength < 0 || fieldDataLength > maxLength) {
                    throw new IllegalArgumentException("Iso8583Field [" + fieldPosition + "] length exceeds " + maxLength);
                }
                switch (fieldLengthEncode) {
                    case Iso8583FieldSupport.ENCODE_BCD:
                        variableLengthString = String.format("%0" + fieldLengthType + "d", fieldDataLength);
                        if (variableLengthString.length() % 2 != 0) {// BCD的长度 左补零
                            variableLengthString = "0" + variableLengthString;
                        }
                        break;
                    case Iso8583FieldSupport.ENCODE_ASCII:
                        variableLengthString = String.format("%0" + fieldLengthType + "d", fieldDataLength);
                        variableLengthString = ByteUtil.string2HexString(variableLengthString);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported length encoding for field " + fieldPosition);
                }
                byte[] variableLengthBytes = ByteUtil.hexString2Bytes(variableLengthString);
                if (Iso8583Log.ENABLED) {
                    log("fieldPosition:" + fieldPosition + " variableLengthBytes: " + variableLengthBytes.length);
                }
                if (variableLengthBytes.length > 0) {
                    System.arraycopy(variableLengthBytes, 0, content, index, variableLengthBytes.length);
                    index += variableLengthBytes.length;
                } else {
                    throw new RuntimeException("Iso8583Field [" + fieldPosition + "]" + " data length error");
                }
            }
            // process data for each field
            byte[] inputBytes = null;
            if (fieldDataEncode == Iso8583FieldSupport.ENCODE_BCD) {
                if (fieldDataBytes != null && fieldDataBytes.length > 0) {
                    inputBytes = fieldDataBytes;
                } else {
                    String paddingString = addPadding(fieldDataString, fieldAlignType, fieldPadding);
                    inputBytes = ByteUtil.hexString2Bytes(paddingString);
                }
            } else if (fieldDataEncode == Iso8583FieldSupport.ENCODE_BIT) {
                if (fieldDataBytes != null && fieldDataBytes.length > 0) {
                    inputBytes = fieldDataBytes;
                } else {
                    String paddingString = addPadding(fieldDataString, fieldAlignType, fieldPadding);
                    inputBytes = ByteUtil.hexString2Bytes(paddingString);
                }
            } else if (fieldDataEncode == Iso8583FieldSupport.ENCODE_ASCII) {
                if (fieldDataBytes != null && fieldDataBytes.length > 0) {
                    inputBytes = fieldDataBytes;
                } else {
                    inputBytes = ByteUtil.asciiString2Bytes(fieldDataString);
                }
            }
            if (inputBytes == null) {
                throw new IllegalArgumentException("Iso8583Field [" + fieldPosition + "] data cannot be null");
            }
            String dataString;
            int encodedDataLength = encodedDataLength(fieldDataEncode, fieldDataLength);
            if (fieldLengthType > Iso8583FieldSupport.LENGTH_VAR_NONE && inputBytes.length != encodedDataLength) {
                throw new IllegalArgumentException("Iso8583Field [" + fieldPosition + "] value does not match its length");
            }
            if (fieldLengthType == Iso8583FieldSupport.LENGTH_VAR_NONE && inputBytes.length > encodedDataLength) {
                throw new IllegalArgumentException("Iso8583Field [" + fieldPosition + "] exceeds fixed length");
            }
            byte[] dataBytes = new byte[encodedDataLength];
            if (Iso8583Log.ENABLED) {
                log("fieldPosition:" + fieldPosition + " dataBytes: " + dataBytes.length);
            }
            dataBytes = addPadding(dataBytes, inputBytes, fieldLengthType, fieldAlignType, fieldPadding,
                    fieldDataEncode);
            if (fieldDataEncode == Iso8583FieldSupport.ENCODE_ASCII && fieldDataString != null && !fieldDataString.isEmpty()) {
                dataString = ByteUtil.bytes2HexString(dataBytes) + " (" + fieldDataString + ")";
            } else {
                dataString = ByteUtil.bytes2HexString(dataBytes);
            }
            if (dataBytes.length > 0) {
                System.arraycopy(dataBytes, 0, content, index, dataBytes.length);
                index += dataBytes.length;
            } else {
                continue;
            }
            // print logs
            if (!variableLengthString.isEmpty()) {
                log("| [" + fieldName + "]: [" + variableLengthString + "] " + dataString + "      [" + desc + "]");
            } else {
                log("| [" + fieldName + "]: [" + fieldDataLength + "] " + dataString + "      [" + desc + "]");
            }
            if (Iso8583Log.ENABLED) {
                log("end index:" + index);
            }
            if (Iso8583Log.ENABLED) {

                log("dataBytes:" + ByteUtil.bytes2HexString(dataBytes));
                log("content:" + ByteUtil.bytes2HexString(content));
            }
            next = entryIterator.hasNext();
        }

        // insert bitmap
        if (hasBitmap) {
            byte[] bitmapBytes = ByteUtil.binaryBytes2Bytes(bitmapBinaryBytes);
            System.arraycopy(bitmapBytes, 0, content, lengthLength + headerLength + msgTypeLength, bitmapBytes.length);
            String bitmapString = ByteUtil.bytes2HexString(bitmapBytes);
            log("| Bitmap: " + bitmapString);
        }

        // length length
        if (lengthLength > 0) {
            byte[] totalLengthLengthBytes = new byte[lengthLength];
            int totalLengthLengthString = content.length - lengthLength;
            String totalLengthLengthHexString = Integer.toHexString(totalLengthLengthString);
            if (totalLengthLengthHexString.length() % 2 != 0) {
                totalLengthLengthHexString = "0" + totalLengthLengthHexString;
            }
            byte[] lengthBytes = ByteUtil.hexString2Bytes(totalLengthLengthHexString);
            if (lengthBytes.length > totalLengthLengthBytes.length) {
                throw new IllegalArgumentException("Message length cannot fit in " + lengthLength + " bytes");
            }
            int offsetIndex = totalLengthLengthBytes.length - 1;
            for (int i = lengthBytes.length - 1; i >= 0; i--) {
                totalLengthLengthBytes[offsetIndex] = lengthBytes[i];
                offsetIndex--;
            }
            System.arraycopy(totalLengthLengthBytes, 0, content, 0, totalLengthLengthBytes.length);
            String lengthString = ByteUtil.bytes2HexString(totalLengthLengthBytes);
            int length = Integer.parseInt(lengthString, 16);
            log("| Length: " + lengthString + " (" + length + ")");
        }

        log("----------------------------------------------------------------");
        log("-----------------ISO8583 encode end-----------------------------");
        log("----------------------------------------------------------------");
        return content;
    }

    /** Returns the actual number of bytes occupied by a field value. */
    private static int encodedDataLength(int dataEncode, int dataLength) {
        if (dataLength < 0) {
            throw new IllegalArgumentException("Iso8583Field dataLength cannot be negative");
        }
        if (dataEncode == Iso8583FieldSupport.ENCODE_BCD) {
            return (dataLength + 1) / 2;
        }
        if (dataEncode == Iso8583FieldSupport.ENCODE_BIT || dataEncode == Iso8583FieldSupport.ENCODE_ASCII) {
            return dataLength;
        }
        throw new IllegalArgumentException("Unsupported data encoding");
    }

    private static void validateFieldPositions(Map<String, Iso8583Field> fieldMap) {
        Set<Integer> positions = new HashSet<>();
        for (Iso8583Field field : fieldMap.values()) {
            if (field == null) {
                throw new IllegalArgumentException("ISO8583 field cannot be null");
            }
            int position = field.getPosition();
            if (position < Iso8583Constant.Position.MIN || position > Iso8583Constant.Position.MAX) {
                throw new IllegalArgumentException("Iso8583Field position must be between 0 and 128: " + position);
            }
            if (!positions.add(position)) {
                throw new IllegalArgumentException("Duplicate field position: " + position);
            }
        }
    }

    /** Detailed messages may contain card data, so logging is opt-in. */
    private static void log(String message) {
        if (Iso8583Log.ENABLED) {
            Log.d(Iso8583Log.TAG, message);
        }
    }

    private static String addPadding(String string, int alignType, String padding) {
        if (string == null) {
            throw new IllegalArgumentException("Iso8583Field data cannot be null");
        }
        if (string.length() % 2 != 0) {
            if (alignType == Iso8583FieldSupport.ALIGN_LEFT) {
                string = string + padding;
            } else {
                string = padding + string;
            }
        }
        return string;
    }

    /**
     * 补齐定长域。ASC 使用填充字符本身的 ASCII 字节；BCD/BIT 使用单个十六进制半字节。
     */
    private static byte[] addPadding(byte[] outBytes, byte[] inputBytes, int lengthType, int alignType,
                                     String padding, int dataEncode) {
        if (lengthType > Iso8583FieldSupport.LENGTH_VAR_NONE) {
            return inputBytes;
        }
        if (inputBytes.length >= outBytes.length) {
            return inputBytes;
        }
        int diff = outBytes.length - inputBytes.length;
        byte[] paddingBytes;
        if (dataEncode == Iso8583FieldSupport.ENCODE_ASCII) {
            if (padding == null || padding.length() != 1) {
                throw new IllegalArgumentException("ASC padding must be exactly one character");
            }
            paddingBytes = new byte[diff];
            byte paddingByte = ByteUtil.asciiString2Bytes(padding)[0];
            for (int i = 0; i < paddingBytes.length; i++) {
                paddingBytes[i] = paddingByte;
            }
        } else {
            if (padding == null || padding.length() != 1) {
                throw new IllegalArgumentException("BCD/BIT padding must be one hex character");
            }
            StringBuilder string = new StringBuilder(diff * 2);
            for (int i = 0; i < diff * 2; i++) {
                string.append(padding);
            }
            paddingBytes = ByteUtil.hexString2Bytes(string.toString());
        }
        if (alignType == Iso8583FieldSupport.ALIGN_LEFT) {
            System.arraycopy(inputBytes, 0, outBytes, 0, inputBytes.length);
            System.arraycopy(paddingBytes, 0, outBytes, inputBytes.length, paddingBytes.length);
        } else {
            System.arraycopy(paddingBytes, 0, outBytes, 0, paddingBytes.length);
            System.arraycopy(inputBytes, 0, outBytes, paddingBytes.length, inputBytes.length);
        }
        return outBytes;
    }

}
