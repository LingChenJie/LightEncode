package com.light.encode

import com.light.encode.util.ByteUtil
import org.junit.Assert.assertEquals
import org.junit.Test
import java.security.Key
import java.util.Arrays
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun test_mac() {
        try {
            val message = "60 01 01 00 00 30 38 30 30 22 20 01 00 00" +
                    "C0 00 01 39 39 30 30 30 30 30 37 32 31 31 32 33" +
                    "31 33 39 30 30 30 30 31 31 38 31 31 36 30 31 30" +
                    "30 31 30 31 30 30 30 30 30 30 30 36 32 31 33 30" +
                    "30 30 33".replace(" ", "")
            val key = "2B 67 17 18 0E E0 8C 20 08 15 78 C8 0C AA DC 0C".replace(" ", "")


            val mac = generateMAC(ByteUtil.hexString2Bytes(message))
            println("Generated MAC: " + bytesToHex(mac))

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val DES_ECB_NO_PADDING = "DES/ECB/NoPadding"
    private val DES_KEY = "2B 67 17 18 0E E0 8C 20 08 15 78 C8 0C AA DC 0C".replace(" ", "").toByteArray() // 16字节密钥


    @Throws(java.lang.Exception::class)
    fun generateMAC(data: ByteArray): ByteArray {
        // 确保数据长度是8的倍数，不足时填充0
        val paddedLength = (data.size + 7) / 8 * 8
        val paddedData = data.copyOf(paddedLength)

        // 拆分16字节密钥为两个8字节的DES密钥
        val key1: ByteArray = Arrays.copyOfRange(DES_KEY, 0, 8)
        val key2: ByteArray = Arrays.copyOfRange(DES_KEY, 8, 16)

        // 创建DES密钥
        val desKey1: Key = SecretKeySpec(key1, "DES")
        val desKey2: Key = SecretKeySpec(key2, "DES")

        // 初始化DES加密器
        val cipher = Cipher.getInstance(DES_ECB_NO_PADDING)

        // 初始化MAC
        var mac = ByteArray(8)

        // 逐块处理数据
        var i = 0
        while (i < paddedData.size) {
            val block = Arrays.copyOfRange(paddedData, i, i + 8)
            // XOR操作
            for (j in mac.indices) {
                mac[j] = (mac[j].toInt() xor block[j].toInt()).toByte()
            }
            // 使用第一个密钥进行DES加密
            cipher.init(Cipher.ENCRYPT_MODE, desKey1)
            mac = cipher.doFinal(mac)
            i += 8
        }

        // 使用第二个密钥进行第二次DES加密
        cipher.init(Cipher.ENCRYPT_MODE, desKey2)
        mac = cipher.doFinal(mac)

        // 使用第一个密钥进行第三次DES加密
        cipher.init(Cipher.ENCRYPT_MODE, desKey1)
        mac = cipher.doFinal(mac)
        return mac
    }

    fun bytesToHex(bytes: ByteArray): String {
        val hexString = StringBuilder(2 * bytes.size)
        for (b in bytes) {
            val hex = Integer.toHexString(0xFF and b.toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString().uppercase(Locale.getDefault())
    }

}