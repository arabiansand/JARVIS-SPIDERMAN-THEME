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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JarvisViewModel(application: Application) : AndroidViewModel(application) {
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

    private val commandRouter = CommandRouter(application)
    
    private val _aiResponse = MutableStateFlow("")
    val aiResponse: StateFlow<String> = _aiResponse.asStateFlow()

    private val voiceManager = VoiceManager(application) { command ->
        processCommand(command)
    }

    val jarvisState: StateFlow<JarvisState> = voiceManager.jarvisState
    val spokenText: StateFlow<String> = voiceManager.spokenText

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            themeRepository.setTheme(theme)
        }
    }

    fun initializeSpeech() {
        voiceManager.initializeSpeechRecognizer()
    }

    fun startListening() {
        voiceManager.startListening()
    }

    fun processCommand(command: String) {
        viewModelScope.launch {
            voiceManager.setThinkingState()
            val result = commandRouter.processCommand(command)
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
