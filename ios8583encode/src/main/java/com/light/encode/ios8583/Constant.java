package com.light.encode.ios8583;

class Constant {

    public static class Position {
        public static final int MIN = 0;
        public static final int MAX = 128;
        public static final int MSG_TYPE = 0;
        public static final int BITMAP = 1;
    }

    public static class AlignType {
        public static final String LEFT = "LEFT";
        public static final String RIGHT = "RIGHT";
    }

    public static class LengthType {
        public static final String NONE = "NONE";
        public static final String PAIR = "PAIR";
        public static final String TRIP = "TRIP";
    }

    public static class EncodeType {
        public static final String BCD = "BCD";
        public static final String BIT = "BIT";
        public static final String ASC = "ASC";
    }

    public static class Field {
        public static final String FIELD = "field";
        public static final String POSITION = "position";
        public static final String LENGTH_ENCODE = "lengthEncode";
        public static final String LENGTH_TYPE = "lengthType";
        public static final String PADDING = "padding";
        public static final String ALIGN_TYPE = "alignType";
        public static final String DATA_ENCODE = "dataEncode";
        public static final String DATA_LENGTH = "dataLength";
        public static final String DATA_BYTES = "dataBytes";
        public static final String DATA_STRING = "dataString";
        public static final String DESC = "desc";
    }


}
