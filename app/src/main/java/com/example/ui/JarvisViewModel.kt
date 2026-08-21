package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.CommandRouter
import com.example.ai.JarvisState
import com.example.ai.VoiceManager
import com.example.data.AppTheme
import com.example.data.CommandRepository
import com.example.data.JarvisDatabase
import com.example.data.ThemePreferencesRepository
import androidx.room.Room
import com.example.util.toBase64
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.graphics.Bitmap

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.util.NetworkConnectivityObserver
import com.example.util.NetworkStatus

class JarvisViewModel(application: Application) : AndroidViewModel(application) {
    private val networkObserver = NetworkConnectivityObserver(application)
    
    val visionModeEnabled = MutableStateFlow(false)
    val captureTrigger = MutableStateFlow(0L)
    private var pendingCommand: String? = null

    fun toggleVisionMode() {
        visionModeEnabled.value = !visionModeEnabled.value
    }

    fun onImageCaptured(bitmap: Bitmap?) {
        val command = pendingCommand ?: return
        pendingCommand = null
        val base64 = bitmap?.toBase64()
        processCommand(command, base64)
    }

    val networkStatus: StateFlow<NetworkStatus> = networkObserver.observe
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NetworkStatus.UNAVAILABLE
        )

    private val db = Room.databaseBuilder(
        application,
        JarvisDatabase::class.java, "jarvis-db"
    ).build()
    
    private val repository = CommandRepository(db.commandMemoryDao())
    val memory = repository.allMemories

    private val themeRepository = ThemePreferencesRepository(application)
    val currentTheme: StateFlow<AppTheme> = themeRepository.themeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.MIDNIGHT_BLUE
        )

    val isHotwordEnabled: StateFlow<Boolean> = themeRepository.hotwordFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val isBackgroundServiceEnabled: StateFlow<Boolean> = themeRepository.backgroundServiceFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val commandRouter = CommandRouter(application)
    
    private val _aiResponse = MutableStateFlow("")
    val aiResponse: StateFlow<String> = _aiResponse.asStateFlow()

    private val voiceManager = VoiceManager(application) { command ->
        if (visionModeEnabled.value) {
            pendingCommand = command
            captureTrigger.value = System.currentTimeMillis()
        } else {
            processCommand(command, null)
        }
    }

    val jarvisState: StateFlow<JarvisState> = voiceManager.jarvisState
    val spokenText: StateFlow<String> = voiceManager.spokenText

    init {
        viewModelScope.launch {
            isHotwordEnabled.collect { enabled ->
                voiceManager.setHotwordEnabled(enabled)
            }
        }
        viewModelScope.launch {
            isBackgroundServiceEnabled.collect { enabled ->
                if (enabled) {
                    com.example.service.JarvisHotwordService.start(application)
                } else {
                    com.example.service.JarvisHotwordService.stop(application)
                }
            }
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            themeRepository.setTheme(theme)
        }
    }

    fun toggleHotwordMode() {
        viewModelScope.launch {
            val nextState = !isHotwordEnabled.value
            themeRepository.setHotwordEnabled(nextState)
            voiceManager.setHotwordEnabled(nextState)
        }
    }

    fun toggleBackgroundService() {
        viewModelScope.launch {
            val nextState = !isBackgroundServiceEnabled.value
            themeRepository.setBackgroundServiceEnabled(nextState)
        }
    }

    fun initializeSpeech() {
        voiceManager.initializeSpeechRecognizer()
    }

    fun startListening() {
        voiceManager.startListening()
    }

    fun processCommand(command: String, base64Image: String? = null) {
        viewModelScope.launch {
            voiceManager.setThinkingState()
            val result = commandRouter.processCommand(command, base64Image)
            _aiResponse.value = result.responseText
            
            // Save to memory
            repository.insertMemory(command, result.responseText)
            
            voiceManager.speak(result.responseText)
        }
    }
    
    fun clearMemory() {
        viewModelScope.launch {
            repository.clearMemory()
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
