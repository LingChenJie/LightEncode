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
        findViewById<Button>(R.id.cashout).setOnClickListener { cashOut() }
        findViewById<Button>(R.id._void).setOnClickListener { void() }
        findViewById<Button>(R.id.sale).setOnClickListener { sale() }
        findViewById<Button>(R.id.settle).setOnClickListener { settle() }
        findViewById<Button>(R.id.logon2).setOnClickListener { logon2() }
        findViewById<Button>(R.id.advice).setOnClickListener { advice() }
        findViewById<Button>(R.id.batchUpload).setOnClickListener { batchUpload() }
        findViewById<Button>(R.id.trailer).setOnClickListener { trailer() }
        findViewById<Button>(R.id.test).setOnClickListener { test() }
        findViewById<Button>(R.id.iso8583).setOnClickListener { iso8583() }
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
            .addField(11, "000011")
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
                "80 08 01 39 39 30 30 30 30 30 37 32 31 31 32 34" +
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
                    .position(53)
                    .lengthType("NONE")
                    .dataLength(0)
                    .build()
            )
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

    private fun cashOut() {
        // 组包
        val encodeBuilder = Iso8583.EncodeBuilder()
            .addHeader("60 01 01 00 00".replace(" ", ""))
            .addMsgType("0200")
            .addField(3, "010000")
            .addField(4, "000000002200")
            .addField(7, "0721123251")
            .addField(11, "000012")
            .addField(12, "19072112351")
            .addField(22, "051")
            .addField(24, "200")
            .addField(25, "00")
            .addField(35, "4170330361279356D220422000000931")
            .addField(41, "60100101")
            .addField(42, "000000062130003")
            .addField(49, "050")
            .addField(52, "43 91 39 CC 5A EF 05 8B".replace(" ", ""))
            .addField(
                55,
                "5F 2A 02 00 50 5F 34 01 01 82 02 3C 00 84 07 A0 00 00 00 03 10 10 95 05 08 80 04 80 00 9A 03 19 07 21 9C 01 00 9F 02 06 00 00 00 00 22 00 9F 09 02 00 8C 9F 10 07 06 01 0A 03 A0 20 00 9F 1A 02 00 50 9F 1E 08 31 37 39 39 37 32 35 37 9F 26 08 FD 4D 21 6C 1E B7 3F 9A 9F 27 01 80 9F 33 03 E0 F0 C8 9F 34 03 42 03 00 9F 35 01 22 9F 36 02 01 57 9F 37 04 2B 28 B1 78"
                    .replace(" ", "")
            )
            .addField(64, "96 42 4B CA 65 9F 28 9D".replace(" ", ""))
            .build()
        val encode = encodeBuilder.encode()
        val encodeHexStr = ByteUtil.bytes2HexString(encode)
        LogUtil.e(TAG, "encodeHexStr: $encodeHexStr")

        // 解包
        val response = "00 A4 60 00 00 01 01 30 32 31 30 F2 30 00 00 0E" +
                "80 82 01 00 00 00 00 04 00 00 00 31 36 34 31 37" +
                "30 33 33 30 33 36 31 32 37 39 33 35 36 30 31 30" +
                "30 30 30 30 30 30 30 30 30 30 30 32 32 30 30 30" +
                "37 32 31 31 32 34 36 35 36 30 30 30 30 31 32 31" +
                "39 30 37 32 31 31 32 33 32 35 31 30 30 30 30 30" +
                "33 37 35 35 33 38 31 30 30 30 30 31 32 30 30 30" +
                "36 30 31 30 30 31 30 31 30 35 30 00 16 91 0A 12" +
                "00 80 5B CC B7 60 70 30 30 8A 02 30 30 38 70 AA" +
                "00 E8 FB B0 39 32 30 35 30 32 31 33 31 30 30 31" +
                "32 31 33 37 30 37"
        val decodeHexStr = response.replace(" ", "")
        val decodeBytes = ByteUtil.hexString2Bytes(encodeHexStr)
        val decodeBuilder = Iso8583.DecodeBuilder()
            .addHeaderLength(5)
            .addDataBytes(decodeBytes)
            .build()
        val decode = decodeBuilder.decode()
        decode.fieldMap.forEach { (position, field) ->
            LogUtil.e(TAG, "position:$position, value:${field.dataString}")
        }
    }

    private fun void() {
        // 组包
        val encodeBuilder = Iso8583.EncodeBuilder()
            .addHeader("60 01 01 00 00".replace(" ", ""))
            .addMsgType("0420")
            .addField(2, "4170330361279356")
            .addField(3, "000000")
            .addField(4, "000000002200")
            .addField(7, "0721123655")
            .addField(11, "000012")
            .addField(12, "19072113251")
            .addField(14, "220430")
            .addField(22, "051")
            .addField(24, "400")
            .addField(25, "00")
            .addField(37, "000003755381")
            .addField(41, "60100101")
            .addField(42, "000000062130003")
            .addField(49, "050")
            .addField(
                55,
                "5F 2A 02 00 50 5F 34 01 01 82 02 3C 00 84 07 A0 00 00 00 03 10 10 95 05 08 80 04 80 00 9A 03 19 07 21 9C 01 00 9F 02 06 00 00 00 00 22 00 9F 09 02 00 8C 9F 10 07 06 01 0A 03 60 20 00 9F 1A 02 00 50 9F 1E 08 31 37 39 39 37 32 35 37 9F 26 08 80 07 BD 79 78 F6 24 96 9F 27 01 40 9F 33 03 E0 F0 C8 9F 34 03 42 03 00 9F 35 01 22 9F 36 02 01 57 9F 37 04 2B 28 B1 78 00 00 00 00"
                    .replace(" ", "")
            )
            .addField(64, "FA 1C 6F E4 61 4A AE FC".replace(" ", ""))
            .build()
        val encode = encodeBuilder.encode()
        val encodeHexStr = ByteUtil.bytes2HexString(encode)
        LogUtil.e(TAG, "encodeHexStr: $encodeHexStr")

        // 解包
        val response = "00 84 60 00 00 01 01 30 34 33 30 72 30 00 00 0E" +
                "80 82 01 31 36 34 31 37 30 33 33 30 33 36 31 32" +
                "37 39 33 35 36 30 30 30 30 30 30 30 30 30 30 30" +
                "30 30 30 32 32 30 30 30 37 32 31 31 32 35 30 34" +
                "32 30 30 30 30 31 32 31 39 30 37 32 31 31 32 33" +
                "32 35 31 30 30 30 30 30 33 37 35 35 33 38 31 30" +
                "30 30 30 31 32 30 30 30 36 30 31 30 30 31 30 31" +
                "30 35 30 00 09 9F 36 02 01 57 8A 02 30 30 B6 04" +
                "8B 39 D1 A8 DD 48 "
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

    private fun sale() {
        // 组包
        val encodeBuilder = Iso8583.EncodeBuilder()
            .addHeader("60 01 01 00 00".replace(" ", ""))
            .addMsgType("0200")
            .addField(3, "000000")
            .addField(4, "000000000100")
            .addField(7, "0721153942")
            .addField(11, "000235")
            .addField(12, "190721153934")
            .addField(22, "051")
            .addField(24, "200")
            .addField(25, "00")
            .addField(35, "4170335864791062D240422000000991")
            .addField(41, "60100101")
            .addField(42, "000000062130003")
            .addField(49, "050")
            .addField(52, "44 E5 B6 27 91 51 A5 93".replace(" ", ""))
            .addField(
                55,
                "5F 2A 02 00 50 5F 34 01 01 82 02 3C 00 84 07 A0 00 00 00 03 10 10 95 05 00 80 04 80 00 9A 03 19 07 21 9C 01 00 9F 02 06 00 00 00 00 01 00 9F 09 02 00 8C 9F 10 07 06 01 0A 03 A0 20 02 9F 1A 02 00 50 9F 1E 08 31 37 39 39 37 32 36 30 9F 26 08 41 09 82 E6 97 5B EB 03 9F 27 01 80 9F 33 03 E0 F0 C8 9F 34 03 42 03 00 9F 35 01 22 9F 36 02 01 1C 9F 37 04 2B ED 20 97"
                    .replace(" ", "")
            )
            .addField(64, "2C D9 4A 26 59 C4 2C A4".replace(" ", ""))
            .build()
        val encode = encodeBuilder.encode()
        val encodeHexStr = ByteUtil.bytes2HexString(encode)
        LogUtil.e(TAG, "encodeHexStr: $encodeHexStr")

        // 解包
        val response = "00 8B 60 00 00 01 01 30 32 31 30 72 30 00 00 0E" +
                "80 82 01 31 36 34 31 37 30 33 33 35 38 36 34 37" +
                "39 31 30 36 32 30 30 30 30 30 30 30 30 30 30 30" +
                "30 30 30 30 31 30 30 30 37 32 31 31 35 35 31 30" +
                "32 30 30 30 32 33 35 31 39 30 37 32 31 31 35 33" +
                "39 33 34 30 30 30 31 34 37 35 34 35 35 34 31 30" +
                "30 30 32 33 35 30 30 30 36 30 31 30 30 31 30 31" +
                "30 35 30 00 16 91 0A CF 5D 9A DB 99 1B 5C 3F 30" +
                "30 8A 02 30 30 B5 D1 25 6E 9B 02 52 CF"
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

    private fun settle() {
        // 组包
        val encodeBuilder = Iso8583.EncodeBuilder()
            .addHeader("60 01 01 00 00".replace(" ", ""))
            .addMsgType("0530")
            .addField(5, "C000000000100")
            .addField(11, "000015")
            .addField(15, "190721")
            .addField(24, "504")
            .addField(41, "60100101")
            .addField(42, "000000062130003")
            .addField(64, "B1 66 59 EA BF 82 6C F3".replace(" ", ""))
            .build()
        val encode = encodeBuilder.encode()
        val encodeHexStr = ByteUtil.bytes2HexString(encode)
        LogUtil.e(TAG, "encodeHexStr: $encodeHexStr")

        // 解包
        val response = "00 4F 60 00 00 01 01 30 35 33 30 08 22 00 00 0E" +
                "80 00 01 43 30 30 30 30 30 30 30 30 30 31 30 30" +
                "30 30 30 30 31 35 31 39 30 37 32 31 30 30 30 30" +
                "30 33 37 35 35 34 30 31 37 35 35 34 30 31 30 30" +
                "30 36 30 31 30 30 31 30 31 F6 AD 1F D6 4B A2 A9" +
                "5F"
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

    private fun logon2() {
        // 组包
        // 解包
        val response2 = "00 49 60 01 01 00 00 30 38 30 30 22 20 01 00 00" +
                "C0 00 01 39 39 30 30 30 30 30 37 32 31 31 32 33" +
                "37 35 39 30 30 30 30 31 36 38 32 31 36 30 31 30" +
                "30 31 30 31 30 30 30 30 30 30 30 36 32 31 33 30" +
                "30 30 33 58 FE C0 71 27 7E 0F 8E"
        val decodeHexStr2 = response2.replace(" ", "")
        val decodeBytes2 = ByteUtil.hexString2Bytes(decodeHexStr2)
        val decodeBuilder2 = Iso8583.DecodeBuilder()
            .addHeaderLength(5)
            .addDataBytes(decodeBytes2)
            .build()
        val decode2 = decodeBuilder2.decode()
        decode2.fieldMap.forEach { (position, field) ->
            LogUtil.e(TAG, "position:$position, value:${field.dataString}")
        }

        // 解包
        val response = "00 46 60 00 00 01 01 30 38 31 30 22 20 00 00 0A" +
                "80 00 01 39 39 30 30 30 30 30 37 32 31 31 32 35" +
                "31 34 32 30 30 30 30 31 36 30 30 30 30 30 33 37" +
                "35 35 34 30 32 30 30 30 36 30 31 30 30 31 30 31 "
        val decodeHexStr = response.replace(" ", "")
        val decodeBytes = ByteUtil.hexString2Bytes(decodeHexStr)
        val decodeBuilder = Iso8583.DecodeBuilder()
            .addHeaderLength(5)
            .addDataBytes(decodeBytes)
            .addFieldConfig(
                Field.Builder()
                    .position(64)
                    .dataLength(0)
                    .build()
            )
            .build()
        val decode = decodeBuilder.decode()
        decode.fieldMap.forEach { (position, field) ->
            LogUtil.e(TAG, "position:$position, value:${field.dataString}")
        }
    }

    private fun advice() {
        // 组包
        val encodeBuilder = Iso8583.EncodeBuilder()
            .addHeader("60 01 01 00 00".replace(" ", ""))
            .addMsgType("0520")
            .addField(5, "C000000000100")
            .addField(11, "000065")
            .addField(15, "171106")
            .addField(24, "504")
            .addField(41, "60100101")
            .addField(42, "000000062130003")
            .addField(64, "CCE276ED691E524E".replace(" ", ""))
            .build()
        val encode = encodeBuilder.encode()
        val encodeHexStr = ByteUtil.bytes2HexString(encode)
        LogUtil.e(TAG, "encodeHexStr: $encodeHexStr")

        // 解包
        val response = "600000037130353330082200040A8000" +
                "01433030303030303135303030303030" +
                "30303635313731313036433030303431" +
                "38323934323738303030303030343638" +
                "3137363039354942424C30313231FFE2" +
                "AB4D16E4490C "
        val decodeHexStr = response.replace(" ", "")
        val decodeBytes = ByteUtil.hexString2Bytes(decodeHexStr)
        val decodeBuilder = Iso8583.DecodeBuilder()
            .addLengthLength(0)
            .addHeaderLength(5)
            .addDataBytes(decodeBytes)
            .build()
        val decode = decodeBuilder.decode()
        decode.fieldMap.forEach { (position, field) ->
            LogUtil.e(TAG, "position:$position, value:${field.dataString}")
        }
    }

    private fun batchUpload() {
        // 组包
        val encodeBuilder = Iso8583.EncodeBuilder()
            .addHeader("60 01 01 00 00".replace(" ", ""))
            .addMsgType("0320")
            .addField(2, "4987720000007486")
            .addField(3, "000000")
            .addField(4, "000000150000")
            .addField(5, "C000000000100")
            .addField(7, "1106142414")
            .addField(11, "000066")
            .addField(12, "171106142352")
            .addField(14, "191230")
            .addField(22, "021")
            .addField(24, "200")
            .addField(25, "00")
            .addField(37, "000000468172")
            .addField(41, "IBBL0121")
            .addField(42, "999000000000009")
            .addField(49, "050")
            .addField(61, "")
            .addField(64, "8074B34C8734CF1D".replace(" ", ""))
            .build()
        val encode = encodeBuilder.encode()
        val encodeHexStr = ByteUtil.bytes2HexString(encode)
        LogUtil.e(TAG, "encodeHexStr: $encodeHexStr")

        // 解包
        val response = "600000037130333330703000000A8000 " +
                "01313634393837373230303030303037 " +
                "34383630303030303030303030303031 " +
                "35303030303030303036363137313130 " +
                "36313432333532303030303030343638 " +
                "3137323030304942424C303132318250 " +
                "24ABF96E32B8 "
        val decodeHexStr = response.replace(" ", "")
        val decodeBytes = ByteUtil.hexString2Bytes(decodeHexStr)
        val decodeBuilder = Iso8583.DecodeBuilder()
            .addLengthLength(0)
            .addHeaderLength(5)
            .addDataBytes(decodeBytes)
            .build()
        val decode = decodeBuilder.decode()
        decode.fieldMap.forEach { (position, field) ->
            LogUtil.e(TAG, "position:$position, value:${field.dataString}")
        }
    }

    private fun trailer() {
        // 组包
        val encodeBuilder = Iso8583.EncodeBuilder()
            .addHeader("60 01 01 00 00".replace(" ", ""))
            .addMsgType("0520")
            .addField(3, "910000")
            .addField(11, "000067")
            .addField(15, "171106")
            .addField(24, "505")
            .addField(41, "IBBL0121")
            .addField(42, "999000000000009")
            .addField(64, "FF6B0F15814A5053".replace(" ", ""))
            .build()
        val encode = encodeBuilder.encode()
        val encodeHexStr = ByteUtil.bytes2HexString(encode)
        LogUtil.e(TAG, "encodeHexStr: $encodeHexStr")

        // 解包
        val response = "600000037130353330202200000A8000   " +
                "01393130303030303030303637313731   " +
                "31303630303030303034363831373830   " +
                "30304942424C30313231AAF201FB3B2A   " +
                "0DA7 "
        val decodeHexStr = response.replace(" ", "")
        val decodeBytes = ByteUtil.hexString2Bytes(decodeHexStr)
        val decodeBuilder = Iso8583.DecodeBuilder()
            .addLengthLength(0)
            .addHeaderLength(5)
            .addDataBytes(decodeBytes)
            .build()
        val decode = decodeBuilder.decode()
        decode.fieldMap.forEach { (position, field) ->
            LogUtil.e(TAG, "position:$position, value:${field.dataString}")
        }
    }

    private fun test() {
        // 解包
        val response = "01146001010000303230303030058020C082013130303030303030303030303030303235303030303334323430363131323235363035303731323030303033323632323537363734303333353331363244333030393230313139343538333331313233343536373831323334353637383930313233343530353001455F2A02005082027C008408A000000333010102950500000000009A032406119C01009F02060000000000259F03060000000000009F090200309F101307010103A00000040A01000000000012362F9C9F1A0200509F1E0830303030303930359F2608810C2DE7176326AB9F2701809F3303E0F8C89F34033F00009F3501229F360200609F37046B862E669F410400000011003938433346373634"
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

    private fun iso8583() {
        Iso8583Config.setBitmapConfig(assets.open("iso8583_example.xml"))
        try {
            var encode = Iso8583.EncodeBuilder()
                .addHeader("6666666666666666666666")
                .addMsgType("0800")
                .addField(2, "6228888888812127121")
                .addField(3, "800008")
                .addField(4, "1")
                .addField(11, "111111")
                .addField(
                    Field.Builder().position(22)
                        .dataBytes(byteArrayOf(1, 2))
                        .build()
                )
                .addField(35, "123D561")
                .addField(41, "10000001")
                .addField(42, "222222222222222")
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