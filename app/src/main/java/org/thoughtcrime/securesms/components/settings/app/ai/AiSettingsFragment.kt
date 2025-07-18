package org.thoughtcrime.securesms.components.settings.app.ai

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.settings.DSLConfiguration
import org.thoughtcrime.securesms.components.settings.DSLSettingsFragment
import org.thoughtcrime.securesms.components.settings.DSLSettingsText
import org.thoughtcrime.securesms.components.settings.configure
import org.thoughtcrime.securesms.keyvalue.AiSummarySettings
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.util.SpanUtil
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class AiSettingsFragment : DSLSettingsFragment(R.string.ai_settings__title) {

  private lateinit var viewModel: AiSettingsViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel = ViewModelProvider(this)[AiSettingsViewModel::class.java]
  }

  override fun bindAdapter(adapter: org.thoughtcrime.securesms.util.adapter.mapping.MappingAdapter) {
    viewModel.state.observe(viewLifecycleOwner) { state ->
      adapter.submitList(getConfiguration(state).toMappingModelList())
    }
  }

  private fun getConfiguration(state: AiSettingsState): DSLConfiguration {
    return configure {
      
      sectionHeaderPref(R.string.ai_settings__ai_summarization)

      textPref(
        title = DSLSettingsText.from(R.string.ai_settings__description),
        summary = DSLSettingsText.from(R.string.ai_settings__description_summary)
      )

      dividerPref()

      switchPref(
        title = DSLSettingsText.from(R.string.ai_settings__enable_ai_summary),
        summary = DSLSettingsText.from(R.string.ai_settings__enable_ai_summary_description),
        isChecked = state.isEnabled,
        onClick = {
          viewModel.setAiEnabled(!state.isEnabled)
        }
      )

      if (state.isEnabled) {
        dividerPref()

        clickPref(
          title = DSLSettingsText.from(R.string.ai_settings__api_key),
          summary = DSLSettingsText.from(
            if (state.apiKey.isNotBlank()) R.string.ai_settings__api_key_configured 
            else R.string.ai_settings__api_key_not_configured
          ),
          onClick = {
            showApiKeyDialog(state.apiKey)
          }
        )

        clickPref(
          title = DSLSettingsText.from(R.string.ai_settings__model_selection),
          summary = DSLSettingsText.from(state.selectedModel),
          onClick = {
            showModelSelectionDialog(state.selectedModel)
          }
        )

        clickPref(
          title = DSLSettingsText.from(R.string.ai_settings__minimum_characters),
          summary = DSLSettingsText.from(getString(R.string.ai_settings__minimum_characters_summary, state.minimumCharacters)),
          onClick = {
            showMinimumCharactersDialog(state.minimumCharacters)
          }
        )
      }
    }
  }

  private fun showApiKeyDialog(currentApiKey: String) {
    val editText = EditText(requireContext()).apply {
      setText(currentApiKey)
      hint = "Enter your Gemini API key"
      setSingleLine(true)
    }

    val layout = LinearLayout(requireContext()).apply {
      orientation = LinearLayout.VERTICAL
      setPadding(64, 16, 64, 16)
      addView(editText)
    }

    MaterialAlertDialogBuilder(requireContext())
      .setTitle(R.string.ai_settings__api_key)
      .setMessage(R.string.ai_settings__api_key_dialog_message)
      .setView(layout)
      .setPositiveButton(android.R.string.ok) { _, _ ->
        viewModel.setApiKey(editText.text.toString().trim())
      }
      .setNegativeButton(android.R.string.cancel, null)
      .show()
  }

  private fun showModelSelectionDialog(currentModel: String) {
    val models = AiSummarySettings.AVAILABLE_MODELS
    val selectedIndex = models.indexOf(currentModel)

    MaterialAlertDialogBuilder(requireContext())
      .setTitle(R.string.ai_settings__model_selection)
      .setSingleChoiceItems(models.toTypedArray(), selectedIndex) { dialog, which ->
        viewModel.setSelectedModel(models[which])
        dialog.dismiss()
      }
      .setNegativeButton(android.R.string.cancel, null)
      .show()
  }

  private fun showMinimumCharactersDialog(currentValue: Int) {
    val options = arrayOf("50", "100", "150", "200", "250")
    val values = arrayOf(50, 100, 150, 200, 250)
    val selectedIndex = values.indexOf(currentValue).takeIf { it >= 0 } ?: 1

    MaterialAlertDialogBuilder(requireContext())
      .setTitle(R.string.ai_settings__minimum_characters)
      .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
        viewModel.setMinimumCharacters(values[which])
        dialog.dismiss()
      }
      .setNegativeButton(android.R.string.cancel, null)
      .show()
  }
}