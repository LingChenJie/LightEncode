/**
 * ISO8583 报文模型、字段配置以及组包/解包入口。
 *
 * <p>调用方通常只需使用 {@link com.light.encode.ios8583.Iso8583Config} 加载字段定义，
 * 再通过 {@link com.light.encode.ios8583.Iso8583Message.EncodeBuilder} 或
 * {@link com.light.encode.ios8583.Iso8583Message.DecodeBuilder} 完成报文转换。</p>
 */
package com.light.encode.ios8583;
