package com.light.encode.demo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MessageTextUtilsTest {

    @Test
    fun parseFields_acceptsUnionPayStyleInput() {
        val fields = MessageTextUtils.parseFields(
            """
                # 银联消费测试数据
                DE3=000000
                4:000000001000
                F11=123456
                41=SUNMI001
            """.trimIndent()
        )

        assertEquals(listOf(3, 4, 11, 41), fields.keys.toList())
        assertEquals("000000001000", fields[4])
    }

    @Test
    fun parseFields_rejectsDuplicateAndReservedFields() {
        assertThrows(IllegalArgumentException::class.java) {
            MessageTextUtils.parseFields("3=000000\nDE3=990000")
        }
        assertThrows(IllegalArgumentException::class.java) {
            MessageTextUtils.parseFields("1=bitmap")
        }
    }

    @Test
    fun hexFormatting_isStableAndValidated() {
        assertEquals("60 01 01 00 00\n02 00", MessageTextUtils.formatHex("60010100000200", 5))
        assertEquals("6001010000", MessageTextUtils.compactHex("60 01 01\n00 00"))
        assertThrows(IllegalArgumentException::class.java) {
            MessageTextUtils.compactHex("60ZZ")
        }
    }

    @Test
    fun sensitiveFields_areMaskedByDefault() {
        assertEquals("622202••••••7890", MessageTextUtils.maskField(2, "6222021234567890", 8))
        assertEquals("••••••••  (8 bytes，默认隐藏)", MessageTextUtils.maskField(52, "439139CC5AEF058B", 8))
    }
}
