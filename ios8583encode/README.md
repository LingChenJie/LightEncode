# ios8583encode 模块说明

`ios8583encode` 是 Android ISO8583/BER-TLV 字节编解码库。它负责字段配置、位图生成、
报文组包和解包，不负责网络通讯、交易流程、密钥管理、PIN 加密或 MAC 计算。

## 1. 模块结构

```text
ios8583encode/
├── build.gradle                         # Android Library、SDK、依赖及 AAR 命名
├── consumer-rules.pro                   # 随 AAR 交付给接入方的混淆规则
└── src/
    ├── main/java/com/light/encode/
    │   ├── ios8583/
    │   │   ├── Iso8583Message.java       # 公开门面、组包/解包 Builder、结果模型
    │   │   ├── Iso8583Field.java         # 字段定义及当前字段值
    │   │   ├── Iso8583Config.java        # XML/List 配置加载与模板管理
    │   │   ├── Iso8583Constant.java      # 对齐、长度类型、编码类型常量
    │   │   ├── Iso8583Encoder.java       # 包内组包实现
    │   │   ├── Iso8583Decoder.java       # 包内解包实现
    │   │   ├── Iso8583FieldSupport.java  # 字段键与配置克隆
    │   │   └── Iso8583Log.java           # 敏感调试日志总开关
    │   ├── tlv/
    │   │   ├── BerTlv.java               # BER-TLV 数据模型
    │   │   └── BerTlvCodec.java          # BER-TLV 编解码
    │   └── util/
    │       ├── ByteUtil.java            # HEX、BCD、ASCII、位图转换
    │       ├── CollectionUtil.java       # 空集合/数组判断
    │       └── RegularUtil.java          # 中文字符检测
    └── test/java/                        # JVM 编解码与异常输入回归测试
```

示例配置位于 app 模块：

- `app/src/main/assets/unionpay_sale.xml`：银联 POS 消费请求/响应字段配置，包含 BCD、
  ASCII 和二进制域的混合编码示例。

## 2. 报文模型

```text
[Length] [Header / TPDU] [MTI] [Primary Bitmap] [Secondary Bitmap?] [Data Elements]
```

| 片段 | 是否可选 | 约定 |
| --- | --- | --- |
| Length | 可选 | `lengthHeaderBytes` 指定字节数；大端二进制整数；值不包含 Length 自身 |
| Header | 可选 | 原始字节；字符串 Builder 入参为十六进制 |
| MTI | 必需 | position=0 的配置决定使用 BCD 还是 ASCII |
| Bitmap | 组包可关闭 | 默认 8 字节；存在 DE65～DE128 时自动生成 16 字节位图 |
| Data Elements | 按位图 | 按字段位置从小到大编码，最大支持 DE128 |

解包器按标准位图定位字段，目前不支持解包 `bitmapEnabled(false)` 生成的非标准无位图报文。

## 3. 快速接入

### 3.1 引入模块

```groovy
dependencies {
    implementation project(':ios8583encode')
}
```

也可以引用 `ios8583encode/build/outputs/aar/` 中生成的 AAR。

### 3.2 加载字段配置

配置是进程级模板，必须在第一次调用组包或默认解包前加载：

```java
Iso8583Config.load(
        context.getAssets().open("unionpay_sale.xml")
);
```

`load(InputStream)` 会关闭传入流。重复调用会原子替换整套配置；同一时刻仅维护一套
全局模板。

### 3.3 组包

```java
byte[] request = new Iso8583Message.EncodeBuilder()
        .lengthHeaderBytes(2)              // 两字节大端二进制报文长度
        .header("6001010000")         // 五字节 TPDU/Header
        .messageType("0200")
        .field(3, "000000")
        .field(4, "000000001000")
        .field(11, "123456")
        .field(22, "051")
        .field(41, "SUNMI001")
        .build()
        .encode();

String requestHex = ByteUtil.bytes2HexString(request);
```

`field(position, value)` 会从全局模板克隆配置。BCD 和 BIT 使用无空格十六进制字符串，
ASCII 使用普通文本。需要传原始字节或临时覆盖配置时，可构建 `Iso8583Field` 后调用
`field(Iso8583Field)`。

### 3.4 解包

```java
Iso8583Message response = new Iso8583Message.DecodeBuilder()
        .data(responseBytes)
        .lengthHeaderBytes(2)
        .headerLength(5)
        .build()
        .decode();

String mti = response.getMessageType();
String processingCode = response.getFieldMap().get("F003").getDataString();
byte[] emvData = response.getFieldMap().get("F055").getDataBytes();
```

如果某个位图置位但字段未配置，解包会立即抛出异常。未知域没有可靠的长度信息，继续解析会
导致后续字段静默错位，因此不能忽略。传给 `decode(Map)` 或 DecodeBuilder 的临时字段配置
也会先复制再使用，不会写入解包后的字段值。

## 4. 字段配置

XML 中每个 `<field>` 对应一个 `Iso8583Field`：

```xml
<field
    position="2"
    lengthEncode="BCD"
    lengthType="PAIR"
    dataLength="19"
    dataEncode="BCD"
    alignType="LEFT"
    padding="0"
    desc="PAN" />
```

| 属性 | 可选值 | 含义 |
| --- | --- | --- |
| `position` | 0～128 | 0=MTI，1=位图保留位，2～128=数据域 |
| `lengthType` | `NONE` / `PAIR` / `TRIP` | XML 兼容值，对应 API 常量 `NONE` / `LLVAR` / `LLLVAR` |
| `lengthEncode` | `BCD` / `ASC` | 变长域长度头编码；定长域不使用 |
| `dataEncode` | `BCD` / `ASC` / `BIT` | 压缩半字节 / US-ASCII / 原始字节 |
| `dataLength` | 非负整数 | 定长域长度或变长域允许的模板长度 |
| `alignType` | `LEFT` / `RIGHT` | 定长域不足时靠左或靠右 |
| `padding` | 单字符 | ASC 补该字符；BCD/BIT 补该十六进制半字节 |
| `desc` | 任意文本 | 调试说明，不参与编码 |

### 长度单位

这是维护时最容易出错的部分：

| 数据编码 | `dataLength` / LLVAR 值 | 实际占用字节数 |
| --- | --- | --- |
| BCD | 数字或十六进制半字节数量 | `(dataLength + 1) / 2` |
| ASC | 字节数 | `dataLength` |
| BIT | 原始字节数 | `dataLength`；字符串形式为两倍数量的 HEX 字符 |

例如 19 位 PAN 的长度头为 19，压缩后占 10 字节；8 字节 PIN Block 的字符串值应为
16 个 HEX 字符，长度仍为 8。

程序化配置建议使用公开常量：

```java
Iso8583Field field = new Iso8583Field.Builder()
        .position(55)
        .lengthType(Iso8583Constant.LengthType.LLLVAR)
        .lengthEncode(Iso8583Constant.EncodeType.BCD)
        .dataEncode(Iso8583Constant.EncodeType.BIT)
        .dataLength(999)
        .alignType(Iso8583Constant.AlignType.LEFT)
        .padding("0")
        .desc("EMV DATA")
        .build();
```

## 5. BER-TLV

DE55 等复合域可使用 `BerTlvCodec`：

```java
Map<String, BerTlv> tags = BerTlvCodec.decode("5A0212349F3303E0F8C8");
String terminalCapabilities = tags.get("9F33").getValue();

BerTlv tlv = new BerTlv("9F33", "E0F8C8");
String encoded = tlv.toHexString(); // 9F3303E0F8C8
```

当前支持 1～3 字节 Tag、确定长度形式和最长 3 字节 Length。返回类型为 Map，因此输入中
存在重复 Tag 时只保留最后一个；需要保留重复 Tag 的业务应在上层使用列表模型。

## 6. API 命名与迁移

本轮整理后的核心类型和方法如下：

| 旧名称 | 新名称 | 说明 |
| --- | --- | --- |
| `Iso8583` | `Iso8583Message` | 明确表示报文模型和编解码入口 |
| `Field` | `Iso8583Field` | 避免与反射、UI 等领域的 Field 混淆 |
| `Constant` | `Iso8583Constant` | 明确常量所属协议 |
| `Iso8583Encode/Decode` | `Iso8583Encoder/Decoder` | 内部职责型名词 |
| `TLV/TLVHelper` | `BerTlv/BerTlvCodec` | 明确 BER-TLV 格式和编解码职责 |
| `setBitmapConfig` | `Iso8583Config.load` | 加载的是整套字段定义，不只是位图 |
| `addLengthLength` | `lengthHeaderBytes` | 明确参数是长度头占用字节数 |
| `addMsgType` | `messageType` | 使用 ISO8583 的 MTI 业务术语 |

旧 Builder 和配置方法暂时保留为 `@Deprecated` 转发方法；旧类型名已经移除，接入方升级 AAR
时需要更新 import。`Iso8583Field` 的类型 getter 现在与 setter 对称返回字符串常量，内部整数
编码不再暴露。

## 7. 错误与安全策略

- 非法 HEX、奇数长度 HEX、字段超长、长度头不匹配、报文截断、未知置位域均抛出
  `IllegalArgumentException`。
- 未加载全局配置或未提供待处理数据等调用状态错误抛出 `IllegalStateException`。
- 详细日志可能包含 PAN、Track 2、PIN Block、DE55 和 MAC，内部日志开关默认关闭；
  生产环境不要开启原始报文日志。
- 本库不验证交易语义，也不代表通过银联认证。域定义、密钥体系、MAC/PIN 算法和通讯头应以
  实际收单机构规范为准。

## 8. 构建与测试

```shell
./gradlew :ios8583encode:test :ios8583encode:lint :ios8583encode:assembleDebug
```

现有回归测试覆盖：

- BCD/ASC/BIT 定长与变长域往返转换。
- 主位图和二级位图生成。
- ASC 定长域字符填充。
- 报文长度头一致性、重复字段配置和 Locale 无关字段键。
- BER-TLV 往返、截断数据、非法 HEX 和非法长度。

新增编码方式、长度规则或字段边界处理时，应同时补充组包和解包的对称测试。

## 9. 维护边界

建议保持以下依赖方向：

```text
业务/app → Iso8583Message + Iso8583Field + Iso8583Config
                    ↓
           Encoder / Decoder / Iso8583FieldSupport
                    ↓
                ByteUtil

业务/app → BerTlv / BerTlvCodec → ByteUtil
```

- `Iso8583Encoder`、`Iso8583Decoder` 和 `Iso8583FieldSupport` 保持包内可见，避免业务绕过门面。
- `Iso8583Field` 同时承担配置和值对象职责，修改长度推导时必须保证组包/解包对称。
- 不在核心库中加入网络、UI、交易状态或密钥存储逻辑。
- 对外 API 变更需考虑已有 AAR 使用方，优先新增能力而不是修改既有含义。
