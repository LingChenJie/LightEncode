package com.light.encode.ios8583;

import com.android.architecture.utils.LogUtils;
import com.light.encode.util.ByteUtil;
import com.light.encode.util.L;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

final class Iso8583Encode {


    public static byte[] encode(Map<String, Field> map, int lengthLength, byte[] header, String msgType, boolean hasBitmap) {
        TreeMap<String, Field> fieldMap = new TreeMap<>(map);

        LogUtils.d(L.TAG, "----------------------------------------------------------------");
        LogUtils.d(L.TAG, "-----------------IOS8583 encode start---------------------------");
        LogUtils.d(L.TAG, "----------------------------------------------------------------");

        // header
        int headerLength = 0;
        if (header != null && header.length > 0) {
            headerLength = header.length;
            String hexString = ByteUtil.bytes2HexString(header);
            LogUtils.d(L.TAG, "| Header: " + hexString);
        }

        // msg type
        int msgTypeLength = 0;
        byte[] msgTypeBytes = new byte[0];

        // calculate all field data and data length
        int bodyLength = 0;
        boolean has128Bitmap = false;
        Iterator<Map.Entry<String, Field>> iterator = fieldMap.entrySet().iterator();
        boolean next = iterator.hasNext();
        while (next) {
            Map.Entry<String, Field> entry = iterator.next();
            Field field = entry.getValue();
            int fieldPosition = field.getPosition();
            int fieldDataEncode = field.getDataEncode();
            int fieldLengthType = field.getLengthType();
            int fieldLengthEncode = field.getLengthEncode();
            int fieldDataLength = field.getDataLength();

            // msg type
            if (fieldPosition == Constant.Position.MSG_TYPE) {
                if (msgType != null && !msgType.isEmpty()) {
                    if (fieldDataEncode == Helper.ENCODE_ASC) {
                        String msgTypeHex = ByteUtil.string2HexString(msgType);
                        msgTypeBytes = ByteUtil.hexString2Bytes(msgTypeHex);
                        LogUtils.d(L.TAG, "| MsgType: " + msgTypeHex + " (" + msgType + ")");
                    } else {
                        msgTypeBytes = ByteUtil.hexString2Bytes(msgType);
                        LogUtils.d(L.TAG, "| MsgType: " + msgType);
                    }
                    msgTypeLength = msgTypeBytes.length;
                }
            }
            // ignore msg type field and bitmap field
            if (fieldPosition == Constant.Position.MSG_TYPE || fieldPosition == Constant.Position.BITMAP) {
                continue;
            }
            // has 128 bitmap
            if (fieldPosition > 64) {
                has128Bitmap = true;
            }
            // calculate the total length of the request data
            int bytesDataLength = 0;
            if (fieldLengthType > Helper.LENGTH_VAR_NONE) {// 变长
                switch (fieldLengthEncode) {
                    case Helper.ENCODE_BCD:
                        bytesDataLength = (fieldLengthType + 1) / 2 + fieldDataLength;
                        break;
                    case Helper.ENCODE_ASC:
                        bytesDataLength = fieldLengthType + fieldDataLength;
                        break;
                }
            } else {// 定长
                bytesDataLength = fieldDataLength;
            }
            if (L.PRINT_DEBUG_MSG) {
                LogUtils.d(L.TAG, "fieldPosition: " + fieldPosition + " bytesDataLength:" + bytesDataLength);
            }
            bodyLength += bytesDataLength;
            next = iterator.hasNext();
        }

        // total length
        int bitmapLength = 0;
        if (hasBitmap) {
            if (has128Bitmap) {
                bitmapLength = 16;
                String name = Helper.getFieldName(1);
                Field field = Helper.getField(1);
                fieldMap.put(name, field);
            } else {
                bitmapLength = 8;
            }
        }
        int totalLength = lengthLength + headerLength + msgTypeLength + bitmapLength + bodyLength;
        if (L.PRINT_DEBUG_MSG) {
            LogUtils.d(L.TAG, "totalLength: " + totalLength + " lengthLength: " + lengthLength + " headerLength: " + headerLength + " msgTypeLength: " + msgTypeLength + " bitmapLength: " + bitmapLength + " bodyLength: " + bodyLength);
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
        Iterator<Map.Entry<String, Field>> entryIterator = fieldMap.entrySet().iterator();
        next = entryIterator.hasNext();
        while (next) {
            if (L.PRINT_DEBUG_MSG) {
                LogUtils.d(L.TAG, "before index:" + index);
            }
            Map.Entry<String, Field> entry = entryIterator.next();
            Field field = entry.getValue();
            int fieldPosition = field.getPosition();
            String fieldName = Helper.getFieldName(fieldPosition);
            String fieldPadding = field.getPadding();
            int fieldAlignType = field.getAlignType();
            int fieldDataEncode = field.getDataEncode();
            int fieldLengthType = field.getLengthType();
            int fieldLengthEncode = field.getLengthEncode();
            int fieldDataLength = field.getDataLength();
            byte[] fieldDataBytes = field.getDataBytes();
            String fieldDataString = field.getDataString();
            String desc = field.getDesc();
            // mark each field
            if (hasBitmap) {
                bitmapBinaryBytes[fieldPosition] = true;
            }
            // ignore msg type field and bitmap field
            if (fieldPosition == Constant.Position.MSG_TYPE || fieldPosition == Constant.Position.BITMAP) {
                continue;
            }
            // calculate the length of variable length data
            String variableLengthString = "";
            if (fieldLengthType > Helper.LENGTH_VAR_NONE) {
                switch (fieldLengthEncode) {
                    case Helper.ENCODE_BCD:
                        variableLengthString = String.format("%0" + fieldLengthType + "d", fieldDataLength);
                        if (variableLengthString.length() % 2 != 0) {// BCD的长度 左补零
                            variableLengthString = "0" + variableLengthString;
                        }
                        break;
                    case Helper.ENCODE_ASC:
                        variableLengthString = String.format("%0" + fieldLengthType + "d", fieldDataLength);
                        variableLengthString = ByteUtil.string2HexString(variableLengthString);
                        break;
                }
                byte[] variableLengthBytes = ByteUtil.hexString2Bytes(variableLengthString);
                if (L.PRINT_DEBUG_MSG) {
                    LogUtils.d(L.TAG, "fieldPosition:" + fieldPosition + " variableLengthBytes: " + variableLengthBytes.length);
                }
                if (variableLengthBytes.length > 0) {
                    System.arraycopy(variableLengthBytes, 0, content, index, variableLengthBytes.length);
                    index += variableLengthBytes.length;
                } else {
                    throw new RuntimeException("Field [" + fieldPosition + "]" + " data length error");
                }
            }
            // process data for each field
            byte[] inputBytes = null;
            if (fieldDataEncode == Helper.ENCODE_BCD) {
                if (fieldDataBytes != null && fieldDataBytes.length > 0) {
                    inputBytes = fieldDataBytes;
                } else {
                    String paddingString = addPadding(fieldDataString, fieldAlignType, fieldPadding);
                    inputBytes = ByteUtil.hexString2Bytes(paddingString);
                }
            } else if (fieldDataEncode == Helper.ENCODE_BIT) {
                if (fieldDataBytes != null && fieldDataBytes.length > 0) {
                    inputBytes = fieldDataBytes;
                } else {
                    String paddingString = addPadding(fieldDataString, fieldAlignType, fieldPadding);
                    inputBytes = ByteUtil.hexString2Bytes(paddingString);
                }
            } else if (fieldDataEncode == Helper.ENCODE_ASC) {
                if (fieldDataBytes != null && fieldDataBytes.length > 0) {
                    inputBytes = fieldDataBytes;
                } else {
                    inputBytes = ByteUtil.asciiString2Bytes(fieldDataString);
                }
            }
            String dataString;
            byte[] dataBytes = new byte[fieldDataLength];
            if (L.PRINT_DEBUG_MSG) {
                LogUtils.d(L.TAG, "fieldPosition:" + fieldPosition + " dataBytes: " + dataBytes.length);
            }
            dataBytes = addPadding(dataBytes, inputBytes, fieldLengthType, fieldAlignType, fieldPadding);
            if (fieldDataEncode == Helper.ENCODE_ASC && fieldDataString != null && !fieldDataString.isEmpty()) {
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
                LogUtils.d(L.TAG, "| [" + fieldName + "]: [" + variableLengthString + "] " + dataString + "      [" + desc + "]");
            } else {
                LogUtils.d(L.TAG, "| [" + fieldName + "]: [" + fieldDataLength + "] " + dataString + "      [" + desc + "]");
            }
            if (L.PRINT_DEBUG_MSG) {
                LogUtils.d(L.TAG, "end index:" + index);
            }
            if (L.PRINT_DEBUG_MSG) {

                LogUtils.d(L.TAG, "dataBytes:" + ByteUtil.bytes2HexString(dataBytes));
                LogUtils.d(L.TAG, "content:" + ByteUtil.bytes2HexString(content));
            }
            next = entryIterator.hasNext();
        }

        // insert bitmap
        if (hasBitmap) {
            byte[] bitmapBytes = ByteUtil.binaryBytes2Bytes(bitmapBinaryBytes);
            System.arraycopy(bitmapBytes, 0, content, lengthLength + headerLength + msgTypeLength, bitmapBytes.length);
            String bitmapString = ByteUtil.bytes2HexString(bitmapBytes);
            LogUtils.d(L.TAG, "| Bitmap: " + bitmapString);
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
            int offsetIndex = totalLengthLengthBytes.length - 1;
            for (int i = lengthBytes.length - 1; i >= 0; i--) {
                totalLengthLengthBytes[offsetIndex] = lengthBytes[i];
                offsetIndex--;
            }
            System.arraycopy(totalLengthLengthBytes, 0, content, 0, totalLengthLengthBytes.length);
            String lengthString = ByteUtil.bytes2HexString(totalLengthLengthBytes);
            int length = Integer.parseInt(lengthString, 16);
            LogUtils.d(L.TAG, "| Length: " + lengthString + " (" + length + ")");
        }

        LogUtils.d(L.TAG, "----------------------------------------------------------------");
        LogUtils.d(L.TAG, "-----------------IOS8583 encode end-----------------------------");
        LogUtils.d(L.TAG, "----------------------------------------------------------------");
        return content;
    }

    private static String addPadding(String string, int alignType, String padding) {
        if (string.length() % 2 != 0) {
            if (alignType == Helper.ALIGN_LEFT) {
                string = string + padding;
            } else {
                string = padding + string;
            }
        }
        return string;
    }

    private static byte[] addPadding(byte[] outBytes, byte[] inputBytes, int lengthType, int alignType, String padding) {
        if (lengthType > Helper.LENGTH_VAR_NONE) {
            return inputBytes;
        }
        if (inputBytes.length >= outBytes.length) {
            return inputBytes;
        }
        StringBuilder string = new StringBuilder();
        int diff = outBytes.length - inputBytes.length;
        for (int i = 0; i < diff * 2; i++) {
            string.append(padding);
        }
        byte[] paddingBytes = ByteUtil.hexString2Bytes(string.toString());
        if (alignType == Helper.ALIGN_LEFT) {
            System.arraycopy(inputBytes, 0, outBytes, 0, inputBytes.length);
            System.arraycopy(paddingBytes, 0, outBytes, inputBytes.length, paddingBytes.length);
        } else {
            System.arraycopy(paddingBytes, 0, outBytes, 0, paddingBytes.length);
            System.arraycopy(inputBytes, 0, outBytes, paddingBytes.length, inputBytes.length);
        }
        return outBytes;
    }

}