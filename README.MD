# Sunmi ISO8583 Encode

Android ISO8583 与 BER-TLV 编解码库，`app` 模块提供常见交易报文示例，核心实现位于
`ios8583encode` 模块。

核心库的目录、报文结构、字段长度约定、接入示例与维护边界见
[`ios8583encode/README.md`](ios8583encode/README.md)。

## app 报文工作台

`app` 是面向开发和联调的 ISO8583 组包/解包工具，内置银联常见业务样例：消费（0200）、
签到（0800）和冲正（0400），同时支持自定义报文域。

- 支持 ASCII POS 与 BCD 基础两套配置，可调整报文头和长度域字节数。
- 组包输入按每行 `DE域号=域值` 编写，例如 `DE3=000000`。
- 解包区支持粘贴连续或带空格、换行的十六进制报文。
- 解包结果分开展示长度、报文头、MTI、位图和各数据域。
- 默认遮罩卡号、二磁道、密码/PIN、IC 卡数据和 MAC 等敏感域；仅调试时可临时显示。

> 内置样例用于验证编解码流程，不包含真实密钥、PIN 或生产 MAC 计算。生产接入仍需遵循
> 收单机构的域定义、密钥体系和通讯规范。

## 构建与验证

```shell
./gradlew test assembleDebug lint
```

生成物：

- 示例 APK：`app/build/outputs/apk/debug/app-debug.apk`
- 库 AAR：`ios8583encode/build/outputs/aar/`

## 长度约定

- BCD 域的 `dataLength` 和 LLVAR/LLLVAR 长度头表示数字位数；实际占用字节数向上取整。
- BIT 与 ASC 域的 `dataLength` 表示字节数。
- `lengthEncode="BCD"` 使用压缩 BCD 长度头，`lengthEncode="ASC"` 使用 ASCII 数字长度头。
