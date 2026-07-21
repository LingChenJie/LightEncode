package com.light.encode.ios8583;

/**
 * ISO8583 字段配置使用的常量。
 *
 * <p>这些值与 XML 配置属性完全一致，也可用于 {@link Iso8583Field.Builder}，避免调用方散落
 * {@code "BCD"}、{@code "PAIR"} 等魔法字符串。</p>
 */
public final class Iso8583Constant {

    private Iso8583Constant() {
        throw new AssertionError("No instances");
    }

    /** ISO8583 中保留位置及合法范围。 */
    public static final class Position {
        public static final int MIN = 0;
        public static final int MAX = 128;
        public static final int MSG_TYPE = 0;
        public static final int BITMAP = 1;

        private Position() {
        }
    }

    /** 定长域不足时的数据对齐方向。 */
    public static final class AlignType {
        public static final String LEFT = "LEFT";
        public static final String RIGHT = "RIGHT";

        private AlignType() {
        }
    }

    /** 固定长度、LLVAR（两位长度）和 LLLVAR（三位长度）。 */
    public static final class LengthType {
        public static final String NONE = "NONE";
        /** 两位十进制长度，即 LLVAR；值保持兼容现有 XML 的 PAIR。 */
        public static final String LLVAR = "PAIR";
        /** 三位十进制长度，即 LLLVAR；值保持兼容现有 XML 的 TRIP。 */
        public static final String LLLVAR = "TRIP";

        /** @deprecated 使用 {@link #LLVAR}。 */
        @Deprecated
        public static final String PAIR = LLVAR;
        /** @deprecated 使用 {@link #LLLVAR}。 */
        @Deprecated
        public static final String TRIP = LLLVAR;

        private LengthType() {
        }
    }

    /** 字段值或变长长度头的编码类型。 */
    public static final class EncodeType {
        public static final String BCD = "BCD";
        public static final String BIT = "BIT";
        /** US-ASCII 字段；值保持兼容现有 XML 的 ASC。 */
        public static final String ASCII = "ASC";

        /** @deprecated 使用 {@link #ASCII}。 */
        @Deprecated
        public static final String ASC = ASCII;

        private EncodeType() {
        }
    }

    /** XML 字段节点及属性名，仅供配置解析器使用。 */
    static final class XmlField {
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

        private XmlField() {
        }
    }


}
