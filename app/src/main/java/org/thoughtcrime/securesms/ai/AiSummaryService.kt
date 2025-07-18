package org.thoughtcrime.securesms.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.signal.core.util.logging.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.generationConfig
import org.thoughtcrime.securesms.R

/**
 * Service for AI text summarization using Google Gemini API
 */
class AiSummaryService private constructor(private val context: Context) {
  
  companion object {
    private val TAG = Log.tag(AiSummaryService::class.java)
    
    @Volatile
    private var INSTANCE: AiSummaryService? = null
    
    fun getInstance(context: Context): AiSummaryService {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: AiSummaryService(context.applicationContext).also { INSTANCE = it }
      }
    }
  }

  private fun createGenerativeModel(apiKey: String, modelName: String): GenerativeModel {
    return GenerativeModel(
      modelName = modelName,
      apiKey = apiKey,
      generationConfig = generationConfig {
        temperature = 0.7f
        maxOutputTokens = 100
      }
    )
  }

  suspend fun summarizeText(text: String): AiSummaryResult {
    return withContext(Dispatchers.IO) {
      try {
        val settings = SignalStore.aiSummary
        
        if (!settings.isConfigured()) {
          Log.w(TAG, "AI summarization not configured")
          return@withContext AiSummaryResult.Error(
            context.getString(R.string.ai_summary__error_no_api_key)
          )
        }

        if (text.length < settings.minimumCharacters) {
          Log.d(TAG, "Text too short for summarization: ${text.length} chars")
          return@withContext AiSummaryResult.Error(
            context.getString(R.string.ai_summary__error, "Text too short")
          )
        }

        val model = createGenerativeModel(settings.apiKey, settings.selectedModel)
        val prompt = createSummarizationPrompt(text)
        
        Log.d(TAG, "Requesting AI summarization for text of length ${text.length}")
        
        val response = model.generateContent(prompt)
        val summary = response.text?.trim()
        
        if (summary.isNullOrBlank()) {
          Log.w(TAG, "AI returned empty summary")
          return@withContext AiSummaryResult.Error(
            context.getString(R.string.ai_summary__error)
          )
        }
        
        Log.d(TAG, "AI summarization successful, summary length: ${summary.length}")
        AiSummaryResult.Success(summary)
        
      } catch (e: Exception) {
        Log.e(TAG, "AI summarization failed", e)
        val errorMessage = when {
          e.message?.contains("API key", ignoreCase = true) == true -> 
            context.getString(R.string.ai_summary__error_no_api_key)
          e.message?.contains("network", ignoreCase = true) == true || 
          e.message?.contains("connection", ignoreCase = true) == true -> 
            context.getString(R.string.ai_summary__error_network)
          else -> context.getString(R.string.ai_summary__error)
        }
        AiSummaryResult.Error(errorMessage)
      }
    }
  }

  private fun createSummarizationPrompt(text: String): String {
    return """
      Please provide a concise summary of the following message in 1-2 sentences. 
      Focus on the main points and keep it brief:
      
      "$text"
      
      Summary:
    """.trimIndent()
  }

  fun canSummarizeText(text: String): Boolean {
    return SignalStore.aiSummary.isConfigured() && 
           text.length >= SignalStore.aiSummary.minimumCharacters
  }
}

sealed class AiSummaryResult {
  data class Success(val summary: String) : AiSummaryResult()
  data class Error(val message: String) : AiSummaryResult()
}