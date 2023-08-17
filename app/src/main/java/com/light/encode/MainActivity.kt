package com.light.encode

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.light.encode.ios8583.Field
import com.light.encode.ios8583.Iso8583
import com.light.encode.ios8583.Iso8583Config
import com.light.encode.tlv.TLVHelper
import com.light.encode.util.ByteUtil
import com.light.encode.utils.LogUtil
import java.nio.charset.StandardCharsets

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
        findViewById<Button>(R.id.iso8583).setOnClickListener {
            iso8583()
        }
    }

    private fun initData() {
        initIso8583()
    }

    private fun initIso8583() {
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
        Iso8583Config.setBitmapConfig(assets.open("bitmap.xml"))
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
//                .addField( MessageField.Builder().position(22).dataBytes( byteArrayOf(33, 34) ).build() )
                .addField(
                    Field.Builder().position(52)
                        .dataString("31323334353637383132333435363738")
                        .dataLength(16)
                        .build()
                )
                .addField(59, "ABC.D")
                .addField(60, "123456781")
                .addField(62, "912345678")
//                .addField(
//                    Field.Builder().position(62)
//                        .dataBytes(ByteUtil.hexString2Bytes("111987654adc")).build()
//                )
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