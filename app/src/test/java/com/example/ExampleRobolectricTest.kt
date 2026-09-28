package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.AiResult
import com.example.ai.OfflineRuleAiEngine
import com.example.model.SupportedLanguages
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
    assertEquals("Maya AI", appName)
  }

  @Test
  fun `test offline ai engine handles urdu greeting`() = runBlocking {
    val engine = OfflineRuleAiEngine()
    val result = engine.generateResponse(
      prompt = "السلام علیکم",
      language = SupportedLanguages.URDU
    )
    assertTrue(result is AiResult.Success)
    val success = result as AiResult.Success
    assertTrue(success.responseText.contains("وعلیکم السلام"))
  }

  @Test
  fun `test offline ai engine math calculation`() = runBlocking {
    val engine = OfflineRuleAiEngine()
    val result = engine.generateResponse(
      prompt = "25 * 4",
      language = SupportedLanguages.ENGLISH
    )
    assertTrue(result is AiResult.Success)
    val success = result as AiResult.Success
    assertTrue(success.responseText.contains("100"))
  }
}
