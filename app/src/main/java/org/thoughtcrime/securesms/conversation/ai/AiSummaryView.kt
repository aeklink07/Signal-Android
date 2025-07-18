package org.thoughtcrime.securesms.conversation.ai

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.ai.AiSummaryResult
import org.thoughtcrime.securesms.ai.AiSummaryService
import org.signal.core.util.logging.Log
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.LifecycleOwner

/**
 * Custom view component for AI message summarization
 */
class AiSummaryView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

  companion object {
    private val TAG = Log.tag(AiSummaryView::class.java)
  }

  private val aiSummaryButton: MaterialButton
  private val aiSummaryContent: LinearLayout
  private val aiSummaryText: TextView
  private val aiSummaryLabel: TextView
  private val aiSummaryLoading: LinearLayout

  private var messageText: String = ""
  private var lifecycleOwner: LifecycleOwner? = null

  init {
    LayoutInflater.from(context).inflate(R.layout.ai_summary_view, this, true)
    
    aiSummaryButton = findViewById(R.id.ai_summary_button)
    aiSummaryContent = findViewById(R.id.ai_summary_content)
    aiSummaryText = findViewById(R.id.ai_summary_text)
    aiSummaryLabel = findViewById(R.id.ai_summary_label)
    aiSummaryLoading = findViewById(R.id.ai_summary_loading)

    aiSummaryButton.setOnClickListener {
      handleSummarizeClick()
    }
  }

  fun setMessageText(text: String) {
    this.messageText = text
    updateVisibility()
  }

  fun setLifecycleOwner(owner: LifecycleOwner) {
    this.lifecycleOwner = owner
  }

  private fun updateVisibility() {
    val service = AiSummaryService.getInstance(context)
    val shouldShow = service.canSummarizeText(messageText)
    
    visibility = if (shouldShow) View.VISIBLE else View.GONE
    
    if (shouldShow) {
      Log.d(TAG, "Showing AI summary button for text of length ${messageText.length}")
    }
  }

  private fun handleSummarizeClick() {
    val owner = lifecycleOwner
    if (owner == null) {
      Log.w(TAG, "No LifecycleOwner set, cannot perform AI summarization")
      return
    }

    Log.d(TAG, "User clicked AI summarize button")
    
    // Show loading state
    aiSummaryButton.visibility = View.GONE
    aiSummaryLoading.visibility = View.VISIBLE
    
    owner.lifecycleScope.launch {
      try {
        val service = AiSummaryService.getInstance(context)
        val result = service.summarizeText(messageText)
        
        withContext(Dispatchers.Main) {
          aiSummaryLoading.visibility = View.GONE
          
          when (result) {
            is AiSummaryResult.Success -> {
              Log.d(TAG, "AI summarization successful")
              showSummary(result.summary)
            }
            is AiSummaryResult.Error -> {
              Log.w(TAG, "AI summarization failed: ${result.message}")
              showError(result.message)
            }
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "Unexpected error during AI summarization", e)
        withContext(Dispatchers.Main) {
          aiSummaryLoading.visibility = View.GONE
          showError(context.getString(R.string.ai_summary__error))
        }
      }
    }
  }

  private fun showSummary(summary: String) {
    aiSummaryText.text = summary
    aiSummaryContent.visibility = View.VISIBLE
    
    // Change button to "Hide Summary" 
    aiSummaryButton.apply {
      text = "Hide Summary"
      setOnClickListener { hideSummary() }
      visibility = View.VISIBLE
    }
  }

  private fun showError(errorMessage: String) {
    aiSummaryText.text = errorMessage
    aiSummaryLabel.text = "Error"
    aiSummaryContent.visibility = View.VISIBLE
    
    // Reset button to original state
    aiSummaryButton.apply {
      text = context.getString(R.string.ai_summary__button_description)
      setOnClickListener { handleSummarizeClick() }
      visibility = View.VISIBLE
    }
  }

  private fun hideSummary() {
    aiSummaryContent.visibility = View.GONE
    
    // Reset button to original state
    aiSummaryButton.apply {
      text = context.getString(R.string.ai_summary__button_description)
      setOnClickListener { handleSummarizeClick() }
    }
  }

  fun reset() {
    aiSummaryContent.visibility = View.GONE
    aiSummaryLoading.visibility = View.GONE
    aiSummaryButton.visibility = View.VISIBLE
    aiSummaryButton.text = context.getString(R.string.ai_summary__button_description)
    aiSummaryButton.setOnClickListener { handleSummarizeClick() }
    aiSummaryLabel.text = "AI Summary"
  }
}