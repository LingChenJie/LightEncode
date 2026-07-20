package com.light.encode.demo

/** Text parsing helpers kept independent from Android so the input format is unit-testable. */
object MessageTextUtils {

    fun parseFields(text: String): LinkedHashMap<Int, String> {
        val result = linkedMapOf<Int, String>()
        text.lineSequence().forEachIndexed { index, source ->
            val line = source.trim()
            if (line.isEmpty() || line.startsWith("#")) return@forEachIndexed

            val separator = line.indexOfFirst { it == '=' || it == ':' }
            require(separator > 0) { "第 ${index + 1} 行格式错误，应为 域号=值" }

            val fieldText = line.substring(0, separator)
                .trim()
                .removePrefix("DE")
                .removePrefix("de")
                .removePrefix("F")
            val position = fieldText.toIntOrNull()
                ?: throw IllegalArgumentException("第 ${index + 1} 行域号无效")
            require(position in 2..128) { "第 ${index + 1} 行域号必须在 2..128 之间" }
            require(!result.containsKey(position)) { "数据域 DE$position 重复" }
            result[position] = line.substring(separator + 1).trim()
        }
        return result
    }

    fun compactHex(text: String): String {
        val compact = text.replace(Regex("\\s+"), "").uppercase()
        require(compact.length % 2 == 0) { "HEX 字符数必须为偶数" }
        require(compact.all { it in '0'..'9' || it in 'A'..'F' }) { "包含非法 HEX 字符" }
        return compact
    }

    fun formatHex(text: String, bytesPerGroup: Int = 8): String {
        val compact = compactHex(text)
        if (compact.isEmpty()) return ""
        return compact.chunked(bytesPerGroup * 2).joinToString("\n") { row ->
            row.chunked(2).joinToString(" ")
        }
    }

    fun maskField(position: Int, value: String, byteLength: Int): String = when (position) {
        2 -> maskAccount(value)
        35 -> maskTrack(value)
        52, 55, 64, 128 -> "••••••••  ($byteLength bytes，默认隐藏)"
        else -> value
    }

    private fun maskAccount(value: String): String {
        if (value.length <= 10) return "•".repeat(value.length)
        return value.take(6) + "•".repeat(value.length - 10) + value.takeLast(4)
    }

    private fun maskTrack(value: String): String {
        val separator = value.indexOfFirst { it == 'D' || it == '=' }
        val pan = if (separator >= 0) value.substring(0, separator) else value
        return maskAccount(pan) + if (separator >= 0) "D••••••••" else ""
    }
}
