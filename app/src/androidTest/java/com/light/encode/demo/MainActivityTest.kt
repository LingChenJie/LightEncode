package com.light.encode.demo

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.hasToString
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Test
    fun defaultUnionPaySalePreset_canEncodeAndDecode() {
        ActivityScenario.launch(MainActivity::class.java).use {
            encodeAndDecode("0200")
            // DE22 是 N3 压缩 BCD，解包结果不应包含高半字节补入的 0。
            onView(withText("051")).check(matches(withText("051")))
        }
    }

    @Test
    fun magneticStripeSalePreset_canEncodeAndDecode() {
        ActivityScenario.launch(MainActivity::class.java).use {
            selectPreset("磁条卡消费请求 · 0200")
            encodeAndDecode("0200")
        }
    }

    @Test
    fun successfulSaleResponsePreset_canEncodeAndDecode() {
        ActivityScenario.launch(MainActivity::class.java).use {
            selectPreset("消费成功响应 · 0210")
            encodeAndDecode("0210")
            onView(withText("00")).check(matches(withText("00")))
        }
    }

    private fun selectPreset(title: String) {
        onView(withId(R.id.presetSpinner)).perform(scrollTo(), click())
        onData(hasToString(title)).perform(click())
    }

    private fun encodeAndDecode(expectedMti: String) {
        onView(withId(R.id.encodeButton)).perform(scrollTo(), click())
        onView(withId(R.id.statusText)).check(matches(withText(containsString("组包成功"))))
        onView(withId(R.id.encodedOutput)).check(
            matches(not(withText(R.string.empty_hex_output)))
        )

        onView(withId(R.id.decodeButton)).perform(scrollTo(), click())
        onView(withId(R.id.statusText)).check(matches(withText(containsString("解包成功"))))
        onView(withId(R.id.decodeMeta)).check(
            matches(withText(containsString("MTI : $expectedMti")))
        )
    }
}
