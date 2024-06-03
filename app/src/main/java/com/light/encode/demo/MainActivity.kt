package com.light.encode.demo

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.light.encode.demo.utils.LogUtil
import com.light.encode.ios8583.Field
import com.light.encode.ios8583.Iso8583
import com.light.encode.ios8583.Iso8583Config
import com.light.encode.tlv.TLVHelper
import com.light.encode.util.ByteUtil

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initData()
    }

    private fun initView() {
        findViewById<Button>(R.id.mac).setOnClickListener { mac() }
        findViewById<Button>(R.id.logon).setOnClickListener { logon() }
    }

    private fun initData() {
        initIso8583Config()
    }

    private fun initIso8583Config() {
//        try {
//            val inputStream = assets.open("bitmap_config.json")
//            val length = inputStream.available()
//            val buffer = ByteArray(length)
//            inputStream.read(buffer)
//            inputStream.close()
//            val string = String(buffer, StandardCharsets.UTF_8)
//            LogUtil.e(TAG, "initIso8583: $string")
//            val type = object : TypeToken<List<Field>>() {}.type
//            val list = Gson().fromJson<List<Field>>(string, type)
//            Iso8583Config.setBitmapConfig(list)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
        Iso8583Config.setBitmapConfig(assets.open("iso8583_example_2.xml"))
    }

    private fun mac() {
        // 组包
        val encodeBuilder = Iso8583.EncodeBuilder()
            .addHeader("60 01 01 00 00".replace(" ", ""))
            .addMsgType("0800")
            .addField(3, "990000")
            .addField(7, "0721123045")
            .addField(11, "000010")
            .addField(24, "815")
            .addField(41, "60100101")
            .addField(42, "000000062130003")
            .build()
        val encode = encodeBuilder.encode()
        val encodeHexStr = ByteUtil.bytes2HexString(encode)
        LogUtil.e(TAG, "encodeHexStr: $encodeHexStr")

        // 解包
        val response = "00 50 60 00 00 01 01 30 38 31 30 22 20 00 00 0A" +
                "80 08 00 39 39 30 30 30 30 30 37 32 31 31 32 34" +
                "34 32 36 30 30 30 30 31 30 30 30 30 30 30 33 37" +
                "35 35 33 37 30 30 30 30 36 30 31 30 30 31 30 31" +
                "31 36 2B 67 17 18 0E E0 8C 20 08 15 78 C8 0C AA" +
                "DC 0C"
        val decodeHexStr = response.replace(" ", "")
        val decodeBytes = ByteUtil.hexString2Bytes(decodeHexStr)
        val decodeBuilder = Iso8583.DecodeBuilder()
            .addHeaderLength(5)
            .addDataBytes(decodeBytes)
            .build()
        val decode = decodeBuilder.decode()
        decode.fieldMap.forEach { (position, field) ->
            LogUtil.e(TAG, "position:$position, value:${field.dataString}")
        }
    }

    private fun logon() {
        // 组包
        val encodeBuilder = Iso8583.EncodeBuilder()
            .addHeader("60 01 01 00 00".replace(" ", ""))
            .addMsgType("0800")
            .addField(3, "990000")
            .addField(7, "0721123139")
            .addField(11, "000010")
            .addField(24, "811")
            .addField(41, "60100101")
            .addField(42, "000000062130003")
            .addField(
                Field.Builder()
                    .position(64)
                    .dataString("32 92 41 24 9C B3 69 BA".replace(" ", ""))
                    .dataLength(8)
                    .build()
            )
            .build()
        val encode = encodeBuilder.encode()
        val encodeHexStr = ByteUtil.bytes2HexString(encode)
        LogUtil.e(TAG, "encodeHexStr: $encodeHexStr")

        // 解包
        val response = "00 58 60 00 00 01 01 30 38 31 30 22 20 00 00 0A" +
                "80 00 01 39 39 30 30 30 30 30 37 32 31 31 32 34" +
                "35 32 32 30 30 30 30 31 31 30 30 30 30 30 33 37" +
                "35 35 33 37 33 30 30 30 36 30 31 30 30 31 30 31" +
                "31 36 EA 70 45 39 11 E5 40 C4 6C 2C FC CF 32 90" +
                "70 8D 46 CA F6 6C B2 8C 15 E9"
        val decodeHexStr = response.replace(" ", "")
        val decodeBytes = ByteUtil.hexString2Bytes(decodeHexStr)
        val decodeBuilder = Iso8583.DecodeBuilder()
            .addHeaderLength(5)
            .addDataBytes(decodeBytes)
            .addFieldConfig(
                Field.Builder()
                    .position(64)
                    .dataLength(26)
                    .build()
            )
            .build()
        val decode = decodeBuilder.decode()
        decode.fieldMap.forEach { (position, field) ->
            LogUtil.e(TAG, "position:$position, value:${field.dataString}")
        }
    }

    private fun iso8583() {
        try {
            var encode = Iso8583.EncodeBuilder()
                .addHeader("6666666666666666666666")
                .addMsgType("0800")
                .addField(2, "6228888888812127121")
                .addField(3, "800008")
                .addField(4, "1")
                .addField(11, "111111")
                .addField(35, "123D561")
                .addField(41, "10000001")
                .addField(42, "222222222222222")
                .addField(
                    Field.Builder().position(22)
                        .dataBytes(byteArrayOf(1, 2))
                        .build()
                )
                .addField(
                    Field.Builder().position(52)
                        .dataString("31323334353637383132333435363738")
                        .dataLength(16)
                        .build()
                )
                .addField(59, "ABC.D")
                .addField(60, "123456781")
//                .addField(62, "912345678")
                .addField(
                    Field.Builder().position(62)
                        .dataBytes(ByteUtil.hexString2Bytes("111987654adc")).build()
                )
                .addField(64, "2173126461641414")
                .addLengthLength(0)
                .build()
                .encode()
            var hexString = ByteUtil.bytes2HexString(encode)
            LogUtil.e(TAG, "hexString: $hexString")

            encode = Iso8583.EncodeBuilder()
                .addHeader("666666666666")
                .addField(4, "000000000999")
                .addField(11, "111111")
                .addField(2, "6228888888812127121")
                .addField(3, "800008")
                .addField(41, "11")
                .addField(42, "222222222222222")
                .addField(60, "123456781")
                .addField(62, "987654321")
                .addField(63, "abc")
                .build()
                .encode()
            hexString = ByteUtil.bytes2HexString(encode)
            Log.e(TAG, "hexString: $hexString")

            encode = Iso8583.EncodeBuilder()
                .addField(4, "000000000999")
                .addField(11, "111111")
                .addField(2, "6228888888812127121")
                .addField(3, "800008")
                .addField(60, "123456781")
                .addField(63, "987654adc")
                .addLengthLength(0)
                .hasBitmap(false)
                .build()
                .encode()
            hexString = ByteUtil.bytes2HexString(encode)
            LogUtil.e(TAG, "hexString: $hexString")

            encode =
                ByteUtil.hexString2Bytes("666666666666666666666608007020000020C00035196228888888812127121080000800000000000111111107123D5610313030303030303132323232323232323232323232323200054142432E4400091234567810000491234567802173126461641414")
            val iso8583 = Iso8583.DecodeBuilder()
                .addLengthLength(0)
                .addHeaderLength(11)
                .addDataBytes(encode)
                .build()
                .decode()
            iso8583.fieldMap.forEach { (key, value) ->
                val dataString = value.dataString
                LogUtil.e(TAG, "$key: $dataString")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initTLV() {
        val hexString =
            "500A4D61737465726361726457105236497927715220D2312201115906785A0852364979277152205F24032312315F2A0208405F340101820239008407A0000000041010950504800080009A032110299C01009F02060000000000159F03009F080200029F090200009F0D05BC509C88009F0E0500000000009F0F05BC709C98009F10120114A00201240000000000000000000000FF9F120A4D6173746572636172649F1A0200009F1E0800000000000000009F26082F697845282A9D089F2701809F3303E0F8C89F34031E03009F3501009F3602001A9F37044E5D02899F50009F5B009F5F009F63009F66009F6B009F6E00DF811700DF811800DF811900DF811B00DF811D00DF811E00DF811F00DF812000DF812100DF812200DF812300DF812400DF812500DF812600DF812900DF812C00";
        val map = TLVHelper.builderMap(hexString)
        map.forEach { (key, value) ->
            val string = value.value
            Log.e(TAG, "$key: $string")
        }
    }

}