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
import com.light.encode.ios8583.Field
import com.light.encode.ios8583.Iso8583
import com.light.encode.ios8583.Iso8583Config
import com.light.encode.util.ByteUtil

/**
 * ISO8583 本地测试工作台。
 *
 * 页面只负责渠道头、字段文本与核心库之间的转换，不包含网络通信或 MAC 计算，
 * 以便清晰验证 Length / Header / MTI / Bitmap / Data Elements 的组包和解包结果。
 */
class MainActivity : AppCompatActivity() {

    private lateinit var profileSpinner: Spinner
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

    private var currentProfile = Profile.ASCII_POS
    private var decodedMessage: Iso8583? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindViews()
        setupSelectors()
        setupActions()
    }

    private fun bindViews() {
        profileSpinner = findViewById(R.id.profileSpinner)
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

    private fun setupSelectors() {
        profileSpinner.adapter = spinnerAdapter(Profile.values().map { it.title })
        presetSpinner.adapter = spinnerAdapter(Preset.values().map { it.title })

        profileSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentProfile = Profile.values()[position]
                loadProfile(currentProfile)
                applyPreset(Preset.values()[presetSpinner.selectedItemPosition])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
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

    private fun loadProfile(profile: Profile) {
        assets.open(profile.asset).use { Iso8583Config.setBitmapConfig(it) }
        setStatus(getString(R.string.status_profile_loaded, profile.title), false)
    }

    private fun applyPreset(preset: Preset) {
        val sample = preset.sample(currentProfile)
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
        loadProfile(currentProfile)
        val header = MessageTextUtils.compactHex(headerInput.text.toString())
        val lengthBytes = parseSmallInt(lengthBytesInput, getString(R.string.label_length_bytes), 0..4)
        val mti = mtiInput.text.toString().trim()
        require(mti.isNotEmpty()) { getString(R.string.error_mti_required) }

        val builder = Iso8583.EncodeBuilder()
            .addLengthLength(lengthBytes)
            .addMsgType(mti)
        if (header.isNotEmpty()) builder.addHeader(header)
        MessageTextUtils.parseFields(fieldsInput.text.toString()).forEach { (position, value) ->
            builder.addField(position, value)
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
        loadProfile(currentProfile)
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

        decodedMessage = Iso8583.DecodeBuilder()
            .addLengthLength(lengthBytes)
            .addHeaderLength(headerLength)
            .addDataBytes(ByteUtil.hexString2Bytes(hex))
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
            message.msgType ?: "—",
            message.bitmap ?: "—"
        )
        val showSensitive = showSensitiveSwitch.isChecked
        message.fieldMap.forEach { (name, field) ->
            decodedFieldsContainer.addView(createFieldView(name, field, showSensitive))
        }
    }

    private fun createFieldView(name: String, field: Field, showSensitive: Boolean): View {
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

    private enum class Profile(val title: String, val asset: String) {
        ASCII_POS("ASCII · POS 调试规范", "iso8583_example_2.xml"),
        BCD_BASIC("BCD · 基础示例规范", "iso8583_example.xml")
    }

    private enum class Preset(val title: String) {
        SALE("消费请求 · 0200"),
        SIGN_IN("签到请求 · 0800"),
        REVERSAL("冲正请求 · 0400"),
        CUSTOM("自定义（保留当前输入）");

        fun sample(profile: Profile): Sample? {
            if (this == CUSTOM) return null
            return if (profile == Profile.ASCII_POS) asciiSample() else bcdSample()
        }

        private fun asciiSample(): Sample = when (this) {
            SALE -> Sample("6001010000", 2, "0200", """
                3=000000
                4=000000001000
                7=0721123045
                11=123456
                22=051
                41=SUNMI001
                42=123456789012345
                49=156
                64=A1B2C3D4E5F60708
            """.trimIndent())
            SIGN_IN -> Sample("6001010000", 2, "0800", """
                3=990000
                7=0721123045
                11=000001
                24=811
                41=SUNMI001
                42=123456789012345
                64=0000000000000000
            """.trimIndent())
            REVERSAL -> Sample("6001010000", 2, "0400", """
                2=6222021234567890
                3=000000
                4=000000001000
                7=0721123045
                11=123456
                22=051
                24=400
                25=00
                37=123456789012
                41=SUNMI001
                42=123456789012345
                49=156
                64=0000000000000000
            """.trimIndent())
            CUSTOM -> error("Custom preset has no sample")
        }

        private fun bcdSample(): Sample = when (this) {
            SALE -> Sample("6001010000", 2, "0200", """
                2=6222021234567890
                3=000000
                4=000000001000
                11=123456
                22=0051
                41=SUNMI001
                42=123456789012345
                64=A1B2C3D4E5F60708
            """.trimIndent())
            SIGN_IN -> Sample("6001010000", 2, "0800", """
                3=990000
                11=000001
                41=SUNMI001
                42=123456789012345
                64=0000000000000000
            """.trimIndent())
            REVERSAL -> Sample("6001010000", 2, "0400", """
                2=6222021234567890
                3=000000
                4=000000001000
                11=123456
                22=0051
                41=SUNMI001
                42=123456789012345
                64=0000000000000000
            """.trimIndent())
            CUSTOM -> error("Custom preset has no sample")
        }
    }

    private data class Sample(
        val header: String,
        val lengthBytes: Int,
        val mti: String,
        val fields: String
    )
}
