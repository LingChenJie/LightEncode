package com.light.encode.demo

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Test
    fun defaultUnionPaySalePreset_canEncodeAndDecode() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.encodeButton)).perform(scrollTo(), click())
            onView(withId(R.id.statusText)).check(matches(withText(containsString("组包成功"))))
            onView(withId(R.id.encodedOutput)).check(
                matches(not(withText(R.string.empty_hex_output)))
            )

            onView(withId(R.id.decodeButton)).perform(scrollTo(), click())
            onView(withId(R.id.statusText)).check(matches(withText(containsString("解包成功"))))
            onView(withId(R.id.decodeMeta)).check(matches(withText(containsString("MTI : 0200"))))
        }
    }
}
