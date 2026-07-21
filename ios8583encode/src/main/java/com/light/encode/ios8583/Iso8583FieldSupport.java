package com.light.encode.ios8583;

import java.util.Locale;
import java.util.Map;

/** 包内字段命名、配置克隆和位图展开工具。 */
final class Iso8583FieldSupport {

    private Iso8583FieldSupport() {
        throw new AssertionError("No instances");
    }

    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_RIGHT = 1;

    public static final int LENGTH_VAR_NONE = 0;
    public static final int LENGTH_LLVAR = 2;
    public static final int LENGTH_LLLVAR = 3;

    public static final int ENCODE_BCD = 0;
    public static final int ENCODE_BIT = 1;
    public static final int ENCODE_ASCII = 2;

    static String fieldName(int position) {
        // 配置键必须与设备语言无关，始终生成 F000～F128 的 ASCII 名称。
        return "F" + String.format(Locale.ROOT, "%03d", position);
    }

    static Iso8583Field emptyField(int position) {
        return new Iso8583Field.Builder().position(position).build();
    }

    static Map<String, Iso8583Field> configSnapshot() {
        return Iso8583Config.fieldConfigSnapshot();
    }

    static Iso8583Field fieldTemplateCopy(String fieldName) {
        Iso8583Field field = Iso8583Config.fieldTemplates().get(fieldName);
        if (field != null) {
            return field.copy();
        }
        return null;
    }

}
