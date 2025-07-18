package org.thoughtcrime.securesms.components.settings.app.ai

data class AiSettingsState(
  val isEnabled: Boolean = false,
  val apiKey: String = "",
  val selectedModel: String = "gemini-1.5-flash",
  val minimumCharacters: Int = 100
)