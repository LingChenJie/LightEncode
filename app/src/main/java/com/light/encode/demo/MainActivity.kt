package com.light.encode.demo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.light.encode.ios8583.Iso8583Field
import com.light.encode.ios8583.Iso8583Message
import com.light.encode.ios8583.Iso8583Config
import com.light.encode.util.ByteUtil

/**
 * ISO8583 本地测试工作台。
 *
 * 页面只负责渠道头、字段文本与核心库之间的转换，不包含网络通信或 MAC 计算，
 * 以便清晰验证 Length / Header / MTI / Bitmap / Data Elements 的组包和解包结果。
 */
class MainActivity : AppCompatActivity() {

    private lateinit var presetSpinner: Spinner
    private lateinit var headerInput: EditText
    private lateinit var lengthBytesInput: EditText
    private lateinit var mtiInput: EditText
    private lateinit var fieldsInput: EditText
    private lateinit var encodedOutput: TextView
    private lateinit var decodeInput: EditText
    private lateinit var decodeHeaderLengthInput: EditText
    private lateinit var decodeLengthBytesInput: EditText
    private lateinit var decodeMeta: TextView
    private lateinit var decodedFieldsContainer: LinearLayout
    private lateinit var showSensitiveSwitch: SwitchCompat
    private lateinit var statusText: TextView

    private var decodedMessage: Iso8583Message? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindViews()
        setupPresetSelector()
        setupActions()
    }

    private fun bindViews() {
        presetSpinner = findViewById(R.id.presetSpinner)
        headerInput = findViewById(R.id.headerInput)
        lengthBytesInput = findViewById(R.id.lengthBytesInput)
        mtiInput = findViewById(R.id.mtiInput)
        fieldsInput = findViewById(R.id.fieldsInput)
        encodedOutput = findViewById(R.id.encodedOutput)
        decodeInput = findViewById(R.id.decodeInput)
        decodeHeaderLengthInput = findViewById(R.id.decodeHeaderLengthInput)
        decodeLengthBytesInput = findViewById(R.id.decodeLengthBytesInput)
        decodeMeta = findViewById(R.id.decodeMeta)
        decodedFieldsContainer = findViewById(R.id.decodedFieldsContainer)
        showSensitiveSwitch = findViewById(R.id.showSensitiveSwitch)
        statusText = findViewById(R.id.statusText)
    }

    /** 初始化消费场景选择器；切换场景只替换页面输入，不会自动发送或保存报文。 */
    private fun setupPresetSelector() {
        presetSpinner.adapter = spinnerAdapter(Preset.values().map { it.title })
        presetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyPreset(Preset.values()[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun spinnerAdapter(items: List<String>): ArrayAdapter<String> =
        ArrayAdapter(this, R.layout.item_spinner, items).also {
            it.setDropDownViewResource(R.layout.item_spinner_dropdown)
        }

    private fun setupActions() {
        findViewById<Button>(R.id.encodeButton).setOnClickListener { encodeMessage() }
        findViewById<Button>(R.id.clearEncodeButton).setOnClickListener {
            fieldsInput.text.clear()
            encodedOutput.text = getString(R.string.empty_hex_output)
            setStatus(getString(R.string.status_ready), false)
        }
        findViewById<Button>(R.id.copyEncodedButton).setOnClickListener {
            copyText(encodedOutput.text.toString().replace("\n", " "))
        }
        findViewById<Button>(R.id.decodeButton).setOnClickListener { decodeMessage() }
        findViewById<Button>(R.id.pasteDecodeButton).setOnClickListener { pasteDecodeInput() }
        findViewById<Button>(R.id.clearDecodeButton).setOnClickListener {
            decodeInput.text.clear()
            decodedMessage = null
            renderDecodedMessage()
        }
        showSensitiveSwitch.setOnCheckedChangeListener { _, _ -> renderDecodedMessage() }
    }

    /**
     * 加载与银联消费字段表对应的唯一配置。
     *
     * 核心库当前使用进程级字段模板，所以每次编解码前重新加载，确保示例不依赖隐式状态。
     */
    private fun loadFieldConfig() {
        assets.open(FIELD_CONFIG_ASSET).use(Iso8583Config::load)
    }

    private fun applyPreset(preset: Preset) {
        val sample = preset.sample()
        if (sample == null) return
        headerInput.setText(sample.header)
        lengthBytesInput.setText(sample.lengthBytes.toString())
        decodeHeaderLengthInput.setText((sample.header.length / 2).toString())
        decodeLengthBytesInput.setText(sample.lengthBytes.toString())
        mtiInput.setText(sample.mti)
        fieldsInput.setText(sample.fields)
        setStatus(getString(R.string.status_preset_loaded, preset.title), false)
    }

    private fun encodeMessage() = runToolAction(getString(R.string.action_encode)) {
        loadFieldConfig()
        val header = MessageTextUtils.compactHex(headerInput.text.toString())
        val lengthBytes = parseSmallInt(lengthBytesInput, getString(R.string.label_length_bytes), 0..4)
        val mti = mtiInput.text.toString().trim()
        require(mti.isNotEmpty()) { getString(R.string.error_mti_required) }

        // Length、Header 与 MTI 不属于普通数据域，需要分别交给 Builder。
        val builder = Iso8583Message.EncodeBuilder()
            .lengthHeaderBytes(lengthBytes)
            .messageType(mti)
        if (header.isNotEmpty()) builder.header(header)
        // DE1 是位图保留位；输入解析器仅允许 DE2～DE128，位图由核心库自动生成。
        MessageTextUtils.parseFields(fieldsInput.text.toString()).forEach { (position, value) ->
            builder.field(position, value)
        }

        val encoded = builder.build().encode()
        val formatted = MessageTextUtils.formatHex(ByteUtil.bytes2HexString(encoded))
        encodedOutput.text = formatted
        decodeInput.setText(formatted)
        decodeHeaderLengthInput.setText((header.length / 2).toString())
        decodeLengthBytesInput.setText(lengthBytes.toString())
        setStatus(getString(R.string.status_encoded, encoded.size), false)
    }

    private fun decodeMessage() = runToolAction(getString(R.string.action_decode)) {
        loadFieldConfig()
        val hex = MessageTextUtils.compactHex(decodeInput.text.toString())
        require(hex.isNotEmpty()) { getString(R.string.error_message_required) }
        val headerLength = parseSmallInt(
            decodeHeaderLengthInput,
            getString(R.string.label_header_length),
            0..128
        )
        val lengthBytes = parseSmallInt(
            decodeLengthBytesInput,
            getString(R.string.label_length_bytes),
            0..4
        )

        // 解包必须明确渠道 Length 和 Header 的字节数，它们无法从位图中推导。
        decodedMessage = Iso8583Message.DecodeBuilder()
            .lengthHeaderBytes(lengthBytes)
            .headerLength(headerLength)
            .data(ByteUtil.hexString2Bytes(hex))
            .build()
            .decode()
        renderDecodedMessage()
        setStatus(
            getString(R.string.status_decoded, decodedMessage?.fieldMap?.size ?: 0),
            false
        )
    }

    private fun renderDecodedMessage() {
        decodedFieldsContainer.removeAllViews()
        val message = decodedMessage
        if (message == null) {
            decodeMeta.text = getString(R.string.empty_decode_result)
            return
        }

        val header = message.header?.let(ByteUtil::bytes2HexString).orEmpty()
        decodeMeta.text = getString(
            R.string.decode_meta_format,
            message.length,
            header.ifEmpty { "—" },
            message.messageType ?: "—",
            message.bitmap ?: "—"
        )
        // PAN、磁道、PIN、IC 数据和 MAC 默认脱敏，避免调试截图意外暴露敏感信息。
        val showSensitive = showSensitiveSwitch.isChecked
        message.fieldMap.forEach { (name, field) ->
            decodedFieldsContainer.addView(createFieldView(name, field, showSensitive))
        }
    }

    private fun createFieldView(name: String, field: Iso8583Field, showSensitive: Boolean): View {
        val view = LayoutInflater.from(this)
            .inflate(R.layout.item_decoded_field, decodedFieldsContainer, false)
        val bytes = field.dataBytes ?: byteArrayOf()
        val position = field.position
        val rawValue = field.dataString.orEmpty()
        val value = if (showSensitive) rawValue else {
            MessageTextUtils.maskField(position, rawValue, bytes.size)
        }
        view.findViewById<TextView>(R.id.fieldNumber).text = name.replace("F", "DE")
        view.findViewById<TextView>(R.id.fieldDescription).text =
            field.desc?.takeIf { it.isNotBlank() } ?: getString(R.string.field_no_description)
        view.findViewById<TextView>(R.id.fieldValue).text = value.ifEmpty { "(empty)" }
        view.findViewById<TextView>(R.id.fieldMeta).text =
            getString(R.string.field_meta_format, field.dataLength, bytes.size)
        return view
    }

    private fun parseSmallInt(input: EditText, name: String, range: IntRange): Int {
        val value = input.text.toString().trim().toIntOrNull()
            ?: throw IllegalArgumentException(getString(R.string.error_number_required, name))
        require(value in range) { getString(R.string.error_number_range, name, range.first, range.last) }
        return value
    }

    private fun pasteDecodeInput() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = clipboard.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString().orEmpty()
        if (text.isBlank()) {
            Toast.makeText(this, R.string.error_clipboard_empty, Toast.LENGTH_SHORT).show()
        } else {
            decodeInput.setText(text)
        }
    }

    private fun copyText(text: String) {
        if (text.isBlank() || text == getString(R.string.empty_hex_output)) return
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.clipboard_hex_label), text))
        Toast.makeText(this, R.string.status_copied, Toast.LENGTH_SHORT).show()
    }

    private inline fun runToolAction(action: String, block: () -> Unit) {
        try {
            block()
        } catch (error: Exception) {
            setStatus(getString(R.string.status_failed, action, error.message ?: error.javaClass.simpleName), true)
        }
    }

    private fun setStatus(message: String, isError: Boolean) {
        statusText.text = message
        statusText.setTextColor(getColor(if (isError) R.color.danger else R.color.success))
    }

    /** 页面预置只描述消费报文，不混入签到、冲正等其他交易规范。 */
    private enum class Preset(val title: String) {
        CHIP_SALE("IC 卡消费请求 · 0200"),
        MAGSTRIPE_SALE("磁条卡消费请求 · 0200"),
        SALE_RESPONSE("消费成功响应 · 0210"),
        CUSTOM("自定义（保留当前输入）");

        fun sample(): Sample? = when (this) {
            CHIP_SALE -> Sample("6001010000", 2, "0200", """
                # IC 卡消费：示例金额为 10.00 元；DE55、PIN Block 和 MAC 均为测试值
                2=6222021234567890
                3=000000
                4=000000001000
                11=123456
                12=143025
                13=0721
                14=2912
                22=051
                23=001
                25=00
                26=12
                41=SUNMI001
                42=123456789012345
                49=156
                52=1234567890ABCDEF
                53=2600000000000000
                55=9F2608A1A2A3A4A5A6A7A89F2701809F36020001950500000000009A032607219C01009F02060000000010005F2A02015682027C009F1A0201569F03060000000000009F3303E0F8C8
                64=0000000000000000
            """.trimIndent())
            MAGSTRIPE_SALE -> Sample("6001010000", 2, "0200", """
                # 磁条卡消费：DE35 使用 D 作为二磁道分隔符
                2=6222021234567890
                3=000000
                4=000000001000
                11=123457
                12=143126
                13=0721
                14=2912
                22=021
                25=00
                26=12
                35=6222021234567890D29122011234567890
                41=SUNMI001
                42=123456789012345
                49=156
                52=1234567890ABCDEF
                53=2600000000000000
                64=0000000000000000
            """.trimIndent())
            SALE_RESPONSE -> Sample("6001010000", 2, "0210", """
                # 消费成功响应：DE39=00；响应 MAC 仍为测试占位值
                3=000000
                4=000000001000
                11=123456
                12=143025
                13=0721
                15=0722
                32=12345678
                37=123456789012
                38=ABC123
                39=00
                41=SUNMI001
                42=123456789012345
                44=CHINA UNIONPAY
                49=156
                64=0000000000000000
            """.trimIndent())
            CUSTOM -> null
        }
    }

    private data class Sample(
        val header: String,
        val lengthBytes: Int,
        val mti: String,
        val fields: String
    )

    private companion object {
        /** 与截图中的消费报文字段表对应；app 不再维护容易混淆的多套配置。 */
        const val FIELD_CONFIG_ASSET = "unionpay_sale.xml"
    }
}
