package com.light.encode.ios8583;

import android.util.Xml;

import com.light.encode.util.CollectionUtil;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 进程级 ISO8583 字段配置仓库。
 *
 * <p>应用启动或切换协议时调用一次 {@link #load(InputStream)}。后续组包/解包
 * 都会取得字段模板的独立副本，报文中的实际值不会写回全局配置。</p>
 *
 * <p>该仓库一次只保存一套配置；需要同时处理多套渠道规范时，应在解包时显式传入配置，
 * 或在业务层串行切换配置。</p>
 */
public final class Iso8583Config {

    private static volatile Map<String, Iso8583Field> originFieldMap = Collections.emptyMap();

    private Iso8583Config() {
        throw new AssertionError("No instances");
    }

    /** 从 XML 流加载并替换当前字段配置；方法结束前会关闭输入流。 */
    public static void load(InputStream input) {
        if (input == null) {
            throw new IllegalArgumentException("ISO8583 config input cannot be null");
        }
        List<Iso8583Field> fields = parseBitmap(input);
        load(fields);
    }

    /** 从字段列表加载并替换当前配置，字段位置不可重复且必须在 0～128。 */
    public static void load(List<Iso8583Field> list) {
        if (CollectionUtil.isEmpty(list)) {
            throw new IllegalArgumentException("ISO8583 field config cannot be empty");
        }
        HashMap<String, Iso8583Field> map = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            Iso8583Field field = list.get(i);
            if (field == null) {
                throw new IllegalArgumentException("Iso8583Field [" + i + "] cannot be null");
            }
            int position = field.getPosition();
            if (position < Iso8583Constant.Position.MIN || position > Iso8583Constant.Position.MAX) {
                throw new IllegalArgumentException("Iso8583Field [" + i + "] out of configuration range.");
            }
            String name = Iso8583FieldSupport.fieldName(position);
            if (map.containsKey(name)) {
                throw new IllegalArgumentException("Duplicate field configuration at position " + position);
            }
            map.put(name, field.copy());
        }
        // 完整构建后再原子替换，避免其他线程读到半成品配置。
        originFieldMap = Collections.unmodifiableMap(map);
    }

    /** 返回可安全写入报文值的配置深拷贝。 */
    public static Map<String, Iso8583Field> fieldConfigSnapshot() {
        if (CollectionUtil.isEmpty(originFieldMap)) {
            throw new IllegalStateException("You must call Iso8583Config.load method first.");
        }
        return copyFields(originFieldMap);
    }

    private static Map<String, Iso8583Field> copyFields(Map<String, Iso8583Field> source) {
        HashMap<String, Iso8583Field> copy = new HashMap<>();
        for (Map.Entry<String, Iso8583Field> entry : source.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().copy());
        }
        return copy;
    }

    /** 返回只读的配置模板，仅供检查配置；报文处理请使用 {@link #fieldConfigSnapshot()}。 */
    public static Map<String, Iso8583Field> fieldTemplates() {
        return Collections.unmodifiableMap(copyFields(originFieldMap));
    }

    /** @deprecated 使用 {@link #load(InputStream)}。 */
    @Deprecated
    public static void setBitmapConfig(InputStream input) {
        load(input);
    }

    /** @deprecated 使用 {@link #load(List)}。 */
    @Deprecated
    public static void setBitmapConfig(List<Iso8583Field> list) {
        load(list);
    }

    /** @deprecated 使用 {@link #fieldConfigSnapshot()}。 */
    @Deprecated
    public static Map<String, Iso8583Field> getFieldMapConfig() {
        return fieldConfigSnapshot();
    }

    /** @deprecated 使用 {@link #fieldTemplates()}。 */
    @Deprecated
    public static Map<String, Iso8583Field> getOriginFieldMap() {
        return fieldTemplates();
    }

    private static List<Iso8583Field> parseBitmap(InputStream input) {
        try {
            List<Iso8583Field> fields = new ArrayList<>();
            Iso8583Field field;
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(input, "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tagName = parser.getName();
                    if (tagName.equals(Iso8583Constant.XmlField.FIELD)) {
                        String position = parser.getAttributeValue(null, Iso8583Constant.XmlField.POSITION);
                        String lengthEncode = parser.getAttributeValue(null, Iso8583Constant.XmlField.LENGTH_ENCODE);
                        String lengthType = parser.getAttributeValue(null, Iso8583Constant.XmlField.LENGTH_TYPE);
                        String dataLength = parser.getAttributeValue(null, Iso8583Constant.XmlField.DATA_LENGTH);
                        String dataEncode = parser.getAttributeValue(null, Iso8583Constant.XmlField.DATA_ENCODE);
                        String alignType = parser.getAttributeValue(null, Iso8583Constant.XmlField.ALIGN_TYPE);
                        String padding = parser.getAttributeValue(null, Iso8583Constant.XmlField.PADDING);
                        String desc = parser.getAttributeValue(null, Iso8583Constant.XmlField.DESC);
                        field = new Iso8583Field.Builder()
                                .position(Integer.parseInt(position))
                                .lengthEncode(lengthEncode)
                                .lengthType(lengthType)
                                .dataLength(Integer.parseInt(dataLength))
                                .dataEncode(dataEncode)
                                .alignType(alignType)
                                .padding(padding)
                                .desc(desc)
                                .build();
                        fields.add(field);
                    }
                }
                eventType = parser.next();
            }
            return fields;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse ISO8583 field config", e);
        } finally {
            try {
                input.close();
            } catch (IOException ignored) {
                // 配置已经完成解析，关闭失败不覆盖更有价值的解析结果或异常。
            }
        }
    }

}
