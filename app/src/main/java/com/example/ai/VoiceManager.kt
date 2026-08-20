package com.example.ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class JarvisState {
    IDLE, LISTENING, THINKING, SPEAKING, ERROR
}

class VoiceManager(private val context: Context, private val onCommandRecognized: (String) -> Unit) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null

    private val _jarvisState = MutableStateFlow(JarvisState.IDLE)
    val jarvisState: StateFlow<JarvisState> = _jarvisState.asStateFlow()

    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText.asStateFlow()

    private var isTtsInitialized = false

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _jarvisState.value = JarvisState.SPEAKING
                }
                override fun onDone(utteranceId: String?) {
                    _jarvisState.value = JarvisState.IDLE
                }
                override fun onError(utteranceId: String?) {
                    _jarvisState.value = JarvisState.ERROR
                }
            })
            isTtsInitialized = true
            speak("As-salaam alaikum. JARVIS online. Systems ready. How can I assist you?")
        }
    }

    fun initializeSpeechRecognizer() {
        if (speechRecognizer != null) return
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { _jarvisState.value = JarvisState.LISTENING }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { _jarvisState.value = JarvisState.THINKING }
                override fun onError(error: Int) {
                    _jarvisState.value = JarvisState.ERROR
                    val errorMsg = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Check mic."
                        SpeechRecognizer.ERROR_CLIENT -> "Speech recognition client error."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network connection issue."
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Tap mic to retry."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Voice engine busy. Please retry."
                        SpeechRecognizer.ERROR_SERVER -> "Voice server unavailable."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Tap mic to retry."
                        else -> "Speech recognition error ($error)"
                    }
                    _spokenText.value = errorMsg
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val command = matches[0]
                        _spokenText.value = command
                        _jarvisState.value = JarvisState.THINKING
                        onCommandRecognized(command)
                    } else {
                        _jarvisState.value = JarvisState.IDLE
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        } else {
            _spokenText.value = "Speech recognition not available on this device."
        }
    }

    fun startListening() {
        if (speechRecognizer == null) {
            _spokenText.value = "Microphone permission required."
            return
        }
        try {
            tts?.stop()
            _spokenText.value = "Listening..."
            _jarvisState.value = JarvisState.LISTENING
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _jarvisState.value = JarvisState.ERROR
            _spokenText.value = "Could not start speech recognition: ${e.localizedMessage}"
        }
    }

    fun speak(text: String) {
        if (isTtsInitialized) {
            _jarvisState.value = JarvisState.SPEAKING
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UtteranceId_${System.currentTimeMillis()}")
        }
    }

    fun setThinkingState() {
        _jarvisState.value = JarvisState.THINKING
    }
    
    fun setIdleState() {
        _jarvisState.value = JarvisState.IDLE
    }

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
    }
}
