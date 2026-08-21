package com.example.ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    IDLE, HOTWORD_STANDBY, LISTENING, THINKING, SPEAKING, ERROR
}

class VoiceManager(
    private val context: Context,
    private val onCommandRecognized: (String) -> Unit
) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _jarvisState = MutableStateFlow(JarvisState.IDLE)
    val jarvisState: StateFlow<JarvisState> = _jarvisState.asStateFlow()

    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText.asStateFlow()

    private val _isHotwordEnabled = MutableStateFlow(true)
    val isHotwordEnabled: StateFlow<Boolean> = _isHotwordEnabled.asStateFlow()

    private var isTtsInitialized = false
    private var isWaitingForFollowUp = false
    private var isHotwordStandbyActive = false

    private val hotwordTriggers = listOf(
        "hey jarvis",
        "okay jarvis",
        "ok jarvis",
        "hi jarvis",
        "hello jarvis",
        "jarvis"
    )

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
                    mainHandler.post {
                        if (isWaitingForFollowUp) {
                            isWaitingForFollowUp = false
                            startListening()
                        } else if (_isHotwordEnabled.value) {
                            startHotwordStandby()
                        } else {
                            _jarvisState.value = JarvisState.IDLE
                        }
                    }
                }
                override fun onError(utteranceId: String?) {
                    mainHandler.post {
                        if (_isHotwordEnabled.value) {
                            startHotwordStandby()
                        } else {
                            _jarvisState.value = JarvisState.ERROR
                        }
                    }
                }
            })
            isTtsInitialized = true
            speak("As-salaam alaikum. JARVIS online. Hotword detection standby. Say Hey JARVIS to activate.")
        }
    }

    fun setHotwordEnabled(enabled: Boolean) {
        _isHotwordEnabled.value = enabled
        if (enabled) {
            if (_jarvisState.value == JarvisState.IDLE || _jarvisState.value == JarvisState.ERROR) {
                startHotwordStandby()
            }
        } else {
            if (isHotwordStandbyActive) {
                stopSpeechRecognizer()
                _jarvisState.value = JarvisState.IDLE
            }
        }
    }

    fun initializeSpeechRecognizer() {
        if (speechRecognizer != null) return
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            mainHandler.post {
                createSpeechRecognizerInstance()
                if (_isHotwordEnabled.value) {
                    startHotwordStandby()
                }
            }
        } else {
            _spokenText.value = "Speech recognition not available on this device."
        }
    }

    private fun createSpeechRecognizerInstance() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    if (!isHotwordStandbyActive) {
                        _jarvisState.value = JarvisState.LISTENING
                    }
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                
                override fun onEndOfSpeech() {
                    if (!isHotwordStandbyActive) {
                        _jarvisState.value = JarvisState.THINKING
                    }
                }

                override fun onError(error: Int) {
                    if (isHotwordStandbyActive) {
                        // Silent retry loop for hotword detection standby
                        scheduleHotwordRetry()
                    } else {
                        _jarvisState.value = JarvisState.ERROR
                        val errorMsg = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Check mic."
                            SpeechRecognizer.ERROR_CLIENT -> "Speech recognition client error."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network connection issue."
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Tap mic to retry."
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Voice engine busy. Retrying..."
                            SpeechRecognizer.ERROR_SERVER -> "Voice server unavailable."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Tap mic to retry."
                            else -> "Speech recognition error ($error)"
                        }
                        _spokenText.value = errorMsg
                        if (_isHotwordEnabled.value) {
                            scheduleHotwordRetry(delayMillis = 1500)
                        }
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val spoken = matches[0]
                        val lower = spoken.lowercase().trim()

                        if (isHotwordStandbyActive) {
                            val matchedHotword = hotwordTriggers.firstOrNull { lower.startsWith(it) || lower.contains(it) }
                            if (matchedHotword != null) {
                                isHotwordStandbyActive = false
                                // Extract any command following the wake word
                                val commandIndex = lower.indexOf(matchedHotword) + matchedHotword.length
                                val trailingCommand = spoken.substring(commandIndex).trim(
                                    ' ', ',', ':', '!', '?'
                                )

                                if (trailingCommand.isNotEmpty() && trailingCommand.length > 2) {
                                    _spokenText.value = "Hey JARVIS > $trailingCommand"
                                    _jarvisState.value = JarvisState.THINKING
                                    onCommandRecognized(trailingCommand)
                                } else {
                                    // Wake word only
                                    _spokenText.value = "Hey JARVIS > Online."
                                    isWaitingForFollowUp = true
                                    speak("Yes, boss?")
                                }
                            } else {
                                // Speech heard without hotword in standby mode; ignore and resume standby
                                scheduleHotwordRetry()
                            }
                        } else {
                            // Direct command capture
                            _spokenText.value = spoken
                            _jarvisState.value = JarvisState.THINKING
                            onCommandRecognized(spoken)
                        }
                    } else {
                        if (isHotwordStandbyActive) {
                            scheduleHotwordRetry()
                        } else if (_isHotwordEnabled.value) {
                            scheduleHotwordRetry()
                        } else {
                            _jarvisState.value = JarvisState.IDLE
                        }
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty() && isHotwordStandbyActive) {
                        val partial = matches[0].lowercase()
                        if (hotwordTriggers.any { partial.contains(it) }) {
                            // Early hotword hit detected
                            _spokenText.value = "Detecting wake trigger..."
                        }
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        } catch (e: Exception) {
            _spokenText.value = "Error initializing speech engine: ${e.localizedMessage}"
        }
    }

    private fun scheduleHotwordRetry(delayMillis: Long = 400) {
        mainHandler.removeCallbacksAndMessages(null)
        mainHandler.postDelayed({
            if (_isHotwordEnabled.value && _jarvisState.value != JarvisState.SPEAKING && _jarvisState.value != JarvisState.THINKING) {
                startHotwordStandby()
            }
        }, delayMillis)
    }

    fun startHotwordStandby() {
        if (!_isHotwordEnabled.value) return
        mainHandler.post {
            try {
                if (speechRecognizer == null) {
                    createSpeechRecognizerInstance()
                }
                tts?.stop()
                isHotwordStandbyActive = true
                _jarvisState.value = JarvisState.HOTWORD_STANDBY

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                scheduleHotwordRetry(1000)
            }
        }
    }

    fun startListening() {
        mainHandler.post {
            try {
                tts?.stop()
                mainHandler.removeCallbacksAndMessages(null)
                isHotwordStandbyActive = false
                _spokenText.value = "Listening for command..."
                _jarvisState.value = JarvisState.LISTENING

                if (speechRecognizer == null) {
                    createSpeechRecognizerInstance()
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                }
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                _jarvisState.value = JarvisState.ERROR
                _spokenText.value = "Could not start speech recognition: ${e.localizedMessage}"
                if (_isHotwordEnabled.value) {
                    scheduleHotwordRetry(1500)
                }
            }
        }
    }

    private fun stopSpeechRecognizer() {
        isHotwordStandbyActive = false
        mainHandler.removeCallbacksAndMessages(null)
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
        } catch (e: Exception) {}
    }

    fun speak(text: String) {
        if (isTtsInitialized) {
            stopSpeechRecognizer()
            _jarvisState.value = JarvisState.SPEAKING
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UtteranceId_${System.currentTimeMillis()}")
        }
    }

    fun setThinkingState() {
        stopSpeechRecognizer()
        _jarvisState.value = JarvisState.THINKING
    }
    
    fun setIdleState() {
        _jarvisState.value = if (_isHotwordEnabled.value) JarvisState.HOTWORD_STANDBY else JarvisState.IDLE
        if (_isHotwordEnabled.value) {
            startHotwordStandby()
        }
    }

    fun destroy() {
        mainHandler.removeCallbacksAndMessages(null)
        tts?.stop()
        tts?.shutdown()
        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {}
    }
}
