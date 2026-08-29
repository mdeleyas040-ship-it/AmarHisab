package com.eleyas.expensetracker.util

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

class VoiceToTextParser(
    private val app: Application
) : RecognitionListener {

    var state by mutableStateOf(VoiceToTextParserState())
        private set

    private val recognizer = SpeechRecognizer.createSpeechRecognizer(app)

    fun startListening(languageCode: String = "bn-BD") {
        state = VoiceToTextParserState(isSpeaking = true)

        if (!SpeechRecognizer.isRecognitionAvailable(app)) {
            state = state.copy(error = "Speech recognition is not available on this device")
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
        }

        recognizer.setRecognitionListener(this)
        recognizer.startListening(intent)
    }

    fun stopListening() {
        state = state.copy(isSpeaking = false)
        recognizer.stopListening()
    }

    override fun onReadyForSpeech(params: Bundle?) {
        state = state.copy(error = null)
    }

    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {
        state = state.copy(isSpeaking = false)
    }

    override fun onError(error: Int) {
        if (error == SpeechRecognizer.ERROR_CLIENT) return
        state = state.copy(
            error = "Error code: $error",
            isSpeaking = false
        )
    }

    override fun onResults(results: Bundle?) {
        results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.getOrNull(0)?.let { result ->
            state = state.copy(spokenText = result)
        }
    }

    override fun onPartialResults(results: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
}

data class VoiceToTextParserState(
    val spokenText: String = "",
    val isSpeaking: Boolean = false,
    val error: String? = null
)
