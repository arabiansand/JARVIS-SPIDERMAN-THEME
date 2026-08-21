package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppTheme
import com.example.data.ThemePreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("JARVIS", appName)
    }

    @Test
    fun `test theme preferences repository hotword flow`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ThemePreferencesRepository(context)
        val initialHotword = repo.hotwordFlow.first()
        // Default is enabled (true)
        assertTrue(initialHotword)
    }
}

