package com.light.encode.ios8583.hide;

import com.light.encode.ios8583.Field;

import java.util.Locale;

public final class Iso8583Helper {

    public static final int ISO_8583_ALIGN_LEFT = 0;
    public static final int ISO_8583_ALIGN_RIGHT = 1;

    public static final int ISO_8583_LEN_VAR_NONE = 0;
    public static final int ISO_8583_LEN_VAR_PAIR = 2;
    public static final int ISO_8583_LEN_VAR_TRIP = 3;

    public static final int ISO_8583_DATA_ENCODE_BCD = 0;
    public static final int ISO_8583_DATA_ENCODE_BIT = 1;
    public static final int ISO_8583_DATA_ENCODE_ASC = 2;

    public static String getMessageFieldName(int position) {
        Locale locale = Locale.getDefault();
        return "F" + String.format(locale, "%03d", position);
    }

    public static Field getMessageField(int position) {
        return new Field.Builder().position(position).build();
    }

    public static String FIELD_CONFIG_128() {
        return "[\n" +
                "\n" +
                "  { \"position\": 0, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 4, \"dataEncode\": \"BCD\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 1, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 8, \"dataEncode\": \"BIT\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "\n" +
                "  { \"position\": 2, \"lengthType\": \"PAIR\", \"lengthEncode\": \"BCD\", \"dataLength\": 19, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 3, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 6, \"dataEncode\": \"BCD\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 4, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 5, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 6, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 7, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 8, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 9, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 10, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 11, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 6, \"dataEncode\": \"BCD\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 12, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 6, \"dataEncode\": \"BCD\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 13, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 4, \"dataEncode\": \"BCD\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 14, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 4, \"dataEncode\": \"BCD\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 15, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 4, \"dataEncode\": \"BCD\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 16, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 17, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 18, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 19, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 20, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 21, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 22, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 3, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 23, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 3, \"dataEncode\": \"BCD\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 24, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 3, \"dataEncode\": \"BCD\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 25, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 2, \"dataEncode\": \"BCD\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 26, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 2, \"dataEncode\": \"BCD\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 27, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 28, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 29, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 30, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 31, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 32, \"lengthType\": \"PAIR\", \"lengthEncode\": \"BCD\", \"dataLength\": 11, \"dataEncode\": \"BCD\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 33, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 34, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 40, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 35, \"lengthType\": \"PAIR\", \"lengthEncode\": \"BCD\", \"dataLength\": 37, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 36, \"lengthType\": \"TRIP\", \"lengthEncode\": \"BCD\", \"dataLength\": 104, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 37, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"ASC\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 38, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 6, \"dataEncode\": \"ASC\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 39, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 2, \"dataEncode\": \"ASC\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 40, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 41, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 8, \"dataEncode\": \"ASC\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 42, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 15, \"dataEncode\": \"ASC\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 43, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 44, \"lengthType\": \"PAIR\", \"lengthEncode\": \"BCD\", \"dataLength\": 25, \"dataEncode\": \"ASC\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 45, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 46, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 47, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 48, \"lengthType\": \"TRIP\", \"lengthEncode\": \"BCD\", \"dataLength\": 322, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 49, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 3, \"dataEncode\": \"ASC\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 50, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 51, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 52, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 8, \"dataEncode\": \"BIT\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 53, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 16, \"dataEncode\": \"BCD\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 54, \"lengthType\": \"TRIP\", \"lengthEncode\": \"BCD\", \"dataLength\": 120, \"dataEncode\": \"ASC\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 55, \"lengthType\": \"TRIP\", \"lengthEncode\": \"BCD\", \"dataLength\": 255, \"dataEncode\": \"BIT\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 56, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 57, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 58, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 59, \"lengthType\": \"TRIP\", \"lengthEncode\": \"BCD\", \"dataLength\": 255, \"dataEncode\": \"ASC\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 60, \"lengthType\": \"TRIP\", \"lengthEncode\": \"BCD\", \"dataLength\": 120, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 61, \"lengthType\": \"TRIP\", \"lengthEncode\": \"BCD\", \"dataLength\": 120, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 62, \"lengthType\": \"TRIP\", \"lengthEncode\": \"BCD\", \"dataLength\": 999, \"dataEncode\": \"BIT\", \"alignType\": \"RIGHT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 63, \"lengthType\": \"TRIP\", \"lengthEncode\": \"BCD\", \"dataLength\": 255, \"dataEncode\": \"ASC\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 64, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 8, \"dataEncode\": \"BIT\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "\n" +
                "  { \"position\": 65, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 66, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 67, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 68, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 69, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 70, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 71, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 72, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 73, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 74, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 75, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 76, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 77, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 78, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 79, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 80, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 81, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 82, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 83, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 84, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 85, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 86, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 87, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 88, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 89, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 90, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 91, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 92, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 93, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 94, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 95, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 96, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 97, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 98, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 99, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 100, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 101, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 102, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 103, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 104, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 105, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 106, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 107, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 108, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 109, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 110, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 111, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 112, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 113, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 114, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 115, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 116, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 117, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 118, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 119, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 120, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 121, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 122, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 123, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 124, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 125, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 126, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 127, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" },\n" +
                "  { \"position\": 128, \"lengthType\": \"NONE\", \"lengthEncode\": \"BCD\", \"dataLength\": 12, \"dataEncode\": \"BCD\", \"alignType\": \"LEFT\", \"padding\": \"0\" }\n" +
                "\n" +
                "]";
    }

}