package org.thoughtcrime.securesms.keyvalue

/**
 * AI Summarization feature settings storage
 */
class AiSummarySettings internal constructor(store: KeyValueStore) : SignalStoreValues(store) {

  companion object {
    private const val AI_SUMMARY_ENABLED = "ai_summary.enabled"
    private const val AI_SUMMARY_API_KEY = "ai_summary.api_key"
    private const val AI_SUMMARY_MODEL = "ai_summary.model"
    private const val AI_SUMMARY_MIN_CHARS = "ai_summary.min_chars"
    
    // Default model options
    const val DEFAULT_MODEL = "gemini-1.5-flash"
    const val DEFAULT_MIN_CHARS = 100
    
    val AVAILABLE_MODELS = listOf(
      "gemini-1.5-flash",
      "gemini-1.5-pro",
      "gemini-1.0-pro"
    )
  }

  var isEnabled: Boolean
    get() = getBoolean(AI_SUMMARY_ENABLED, false)
    set(value) = putBoolean(AI_SUMMARY_ENABLED, value)

  var apiKey: String
    get() = getString(AI_SUMMARY_API_KEY, "")
    set(value) = putString(AI_SUMMARY_API_KEY, value)

  var selectedModel: String
    get() = getString(AI_SUMMARY_MODEL, DEFAULT_MODEL)
    set(value) = putString(AI_SUMMARY_MODEL, value)

  var minimumCharacters: Int
    get() = getInteger(AI_SUMMARY_MIN_CHARS, DEFAULT_MIN_CHARS)
    set(value) = putInteger(AI_SUMMARY_MIN_CHARS, value)

  fun isConfigured(): Boolean {
    return isEnabled && apiKey.isNotBlank()
  }

  override fun onFirstEverAppLaunch() {
    // Set defaults for first app launch
  }

  override fun getKeysToIncludeInBackup(): List<String> {
    return listOf(
      AI_SUMMARY_ENABLED,
      AI_SUMMARY_MODEL,
      AI_SUMMARY_MIN_CHARS
      // Note: We don't backup the API key for security reasons
    )
  }
}