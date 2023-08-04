package com.light.encode.ios8583.hide;

import com.light.encode.ios8583.Iso8583Config;
import com.light.encode.ios8583.Iso8583;
import com.light.encode.ios8583.Field;
import com.light.encode.util.ByteUtil;
import com.light.encode.util.RegularUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Iso8583Decode {

    public static Iso8583 decode(byte[] bytes, int totalLengthLength, int headerLength, int messageLength, Map<String, Field> fieldConfigMap) throws Exception {
        Iso8583.Builder builder = new Iso8583.Builder();

        Logger.getLogger(Iso8583Config.TAG).log(Level.INFO, "========================Ios8583-Decode-Start========================");

        // header
        if (headerLength > 0) {
            byte[] headerBytes = new byte[headerLength];
            System.arraycopy(bytes, totalLengthLength, headerBytes, 0, headerLength);
            String header = ByteUtil.bytes2HexString(headerBytes);
            Logger.getLogger(Iso8583Config.TAG).log(Level.INFO, "|| Header: " + header);
            builder.addHeader(headerBytes);
        }

        // message
        if (messageLength > 0) {
            byte[] messageBytes = new byte[messageLength];
            System.arraycopy(bytes, totalLengthLength + headerLength, messageBytes, 0, messageLength);
            String message = ByteUtil.bytes2HexString(messageBytes);
            Logger.getLogger(Iso8583Config.TAG).log(Level.INFO, "|| Message: " + message);
            builder.addMessage(messageBytes);
        }

        // bitmap
        int bitmapLength = 8;
        byte[] bitmapBytes = new byte[bitmapLength];
        System.arraycopy(bytes, totalLengthLength + headerLength + messageLength, bitmapBytes, 0, bitmapLength);
        boolean[] bitmapBooleans = ByteUtil.bytes2BinaryBytes(bitmapBytes);
        if (bitmapBooleans[1]) { // 如果第一位为1, 则是可扩展位图
            bitmapLength = 16;
            bitmapBytes = new byte[bitmapLength];
            System.arraycopy(bytes, totalLengthLength + headerLength + messageLength, bitmapBytes, 0, bitmapLength);
            bitmapBooleans = ByteUtil.bytes2BinaryBytes(bitmapBytes);
        }

        // body - all field
        HashMap<String, Field> fieldMap = new HashMap<>();
        int index = totalLengthLength + headerLength + messageLength + bitmapLength;
        for (int i = 2; i < bitmapBooleans.length; i++) { // 0 1 field be not used
            boolean bool = bitmapBooleans[i];
            if (bool == false) continue;
            String name = Iso8583Helper.getMessageFieldName(i);
            Field field = fieldConfigMap.get(name);
            if (field == null) continue;
            int fieldAlignType = field.getAlignType();
            int fieldDataEncode = field.getDataEncode();
            int fieldDataLength = field.getDataLength();
            int fieldLengthType = field.getLengthType();
            // handle length
            int dataLength = 0;
            int bytesDataLength = 0;
            String dataLengthString;
            if (fieldLengthType > Iso8583Helper.ISO_8583_LEN_VAR_NONE) {
                // handle byte array of length
                int variableLength = (fieldLengthType + 1) / 2;
                byte[] variableLengthBytes = new byte[variableLength];
                System.arraycopy(bytes, index, variableLengthBytes, 0, variableLength);
                dataLength = ByteUtil.bcd2Int(variableLengthBytes);
                dataLengthString = String.format("%0" + fieldLengthType + "d", dataLength);
                if (dataLengthString.length() % 2 != 0) {
                    dataLengthString = "0" + dataLengthString;// 长度前补零
                }
                if (fieldDataEncode == Iso8583Helper.ISO_8583_DATA_ENCODE_BCD) {
                    bytesDataLength = dataLength / 2 + dataLength % 2;
                } else if (fieldDataEncode == Iso8583Helper.ISO_8583_DATA_ENCODE_BIT) {
                    bytesDataLength = dataLength;
                } else if (fieldDataEncode == Iso8583Helper.ISO_8583_DATA_ENCODE_ASC) {
                    bytesDataLength = dataLength;
                }
                index = index + variableLength;
            } else {
                if (fieldDataEncode == Iso8583Helper.ISO_8583_DATA_ENCODE_BCD) {
                    bytesDataLength = fieldDataLength / 2 + fieldDataLength % 2;
                } else if (fieldDataEncode == Iso8583Helper.ISO_8583_DATA_ENCODE_BIT) {
                    bytesDataLength = fieldDataLength;
                } else if (fieldDataEncode == Iso8583Helper.ISO_8583_DATA_ENCODE_ASC) {
                    bytesDataLength = fieldDataLength;
                }
                dataLengthString = String.valueOf(fieldDataLength);
            }
            byte[] dataBytes = new byte[bytesDataLength];
            System.arraycopy(bytes, index, dataBytes, 0, bytesDataLength);
            index = index + bytesDataLength;
            // handle data
            String dataString = ByteUtil.bytes2HexString(dataBytes);
            if (fieldDataEncode == Iso8583Helper.ISO_8583_DATA_ENCODE_ASC) {
                String gbkString = ByteUtil.hexString2GBKString(dataString);
                boolean isContainZH = RegularUtil.isContainChinese(gbkString);
                if (isContainZH) {
                    dataString = gbkString;
                } else {
                    dataString = ByteUtil.asciiBytes2String(dataBytes);
                }
            }
            field.setDataString(dataString);
            field.setDataBytes(dataBytes);
            if (fieldLengthType > Iso8583Helper.ISO_8583_LEN_VAR_NONE) {
                if (fieldDataEncode == Iso8583Helper.ISO_8583_DATA_ENCODE_BCD) {
                    if (fieldAlignType == Iso8583Helper.ISO_8583_ALIGN_LEFT) {
                        dataString = dataString.substring(0, dataLength);
                    } else {
                        int length = dataString.length();
                        dataString = dataString.substring(length - dataLength, length);
                    }
                } else if (fieldDataEncode == Iso8583Helper.ISO_8583_DATA_ENCODE_BIT) {
                    if (fieldAlignType == Iso8583Helper.ISO_8583_ALIGN_LEFT) {
                        dataString = dataString.substring(0, dataLength * 2);
                    } else {
                        int length = dataString.length();
                        dataString = dataString.substring(length - dataLength * 2, length);
                    }
                }
                field.setDataString(dataString);
                field.setDataLength(dataLength);
            }
            fieldMap.put(name, field);
            Logger.getLogger(Iso8583Config.TAG).log(Level.INFO, "|| Field [" + i + "]: [" + dataLengthString + "] " + dataString);
        }
        builder.addMessageField(fieldMap);

        byte[] allFieldDataBytes = new byte[bytes.length - totalLengthLength - headerLength - messageLength];
        System.arraycopy(bytes, totalLengthLength + headerLength + messageLength, allFieldDataBytes, 0, allFieldDataBytes.length);
        builder.addAllFieldData(allFieldDataBytes);

        String bitmapString = ByteUtil.bytes2HexString(bitmapBytes);
        Logger.getLogger(Iso8583Config.TAG).log(Level.INFO, "|| Bitmap: " + bitmapString);
        builder.addBitmap(bitmapString);

        if (totalLengthLength > 0) {
            byte[] lengthBytes = new byte[totalLengthLength];
            System.arraycopy(bytes, 0, lengthBytes, 0, totalLengthLength);
            String lengthString = ByteUtil.bytes2HexString(lengthBytes);
            int length = Integer.parseInt(lengthString, 16);
            Logger.getLogger(Iso8583Config.TAG).log(Level.INFO, "|| Length: " + lengthString + " (" + length + ")");
            builder.addLength(length);
        }

        Logger.getLogger(Iso8583Config.TAG).log(Level.INFO, "=========================Ios8583-Decode-End=========================");

        return builder.build();
    }

}