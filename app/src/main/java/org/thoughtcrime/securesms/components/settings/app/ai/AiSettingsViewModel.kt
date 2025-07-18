package org.thoughtcrime.securesms.components.settings.app.ai

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.util.livedata.Store

class AiSettingsViewModel : ViewModel() {

  private val store = Store(getCurrentState())

  val state: LiveData<AiSettingsState> = store.stateLiveData

  private fun getCurrentState(): AiSettingsState {
    return AiSettingsState(
      isEnabled = SignalStore.aiSummary.isEnabled,
      apiKey = SignalStore.aiSummary.apiKey,
      selectedModel = SignalStore.aiSummary.selectedModel,
      minimumCharacters = SignalStore.aiSummary.minimumCharacters
    )
  }

  fun setAiEnabled(enabled: Boolean) {
    SignalStore.aiSummary.isEnabled = enabled
    store.update { it.copy(isEnabled = enabled) }
  }

  fun setApiKey(apiKey: String) {
    SignalStore.aiSummary.apiKey = apiKey
    store.update { it.copy(apiKey = apiKey) }
  }

  fun setSelectedModel(model: String) {
    SignalStore.aiSummary.selectedModel = model
    store.update { it.copy(selectedModel = model) }
  }

  fun setMinimumCharacters(minChars: Int) {
    SignalStore.aiSummary.minimumCharacters = minChars
    store.update { it.copy(minimumCharacters = minChars) }
  }
}