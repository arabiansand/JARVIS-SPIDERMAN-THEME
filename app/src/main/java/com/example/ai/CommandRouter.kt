package com.example.ai

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.Settings
import android.widget.Toast
import com.example.api.RetrofitClient
import com.example.api.GenerateContentRequest
import com.example.api.Content
import com.example.api.Part
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.BuildConfig

enum class CommandType {
    CHAT, APP_LAUNCH, ALARM, SYSTEM_INFO
}

data class CommandResult(val type: CommandType, val responseText: String)

class CommandRouter(private val context: Context) {
    
    suspend fun processCommand(command: String): CommandResult {
        val lowerCommand = command.lowercase()
        
        // Simple heuristic routing before hitting AI (for fast responses)
        if (lowerCommand.contains("open youtube")) {
            launchApp("com.google.android.youtube")
            return CommandResult(CommandType.APP_LAUNCH, "Launching YouTube.")
        }
        if (lowerCommand.contains("open chrome")) {
            launchApp("com.android.chrome")
            return CommandResult(CommandType.APP_LAUNCH, "Launching Chrome.")
        }
        if (lowerCommand.contains("open settings")) {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            return CommandResult(CommandType.APP_LAUNCH, "Opening Settings.")
        }
        if (lowerCommand.contains("set an alarm") || lowerCommand.contains("set alarm")) {
            // Simplified alarm dummy
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(intent)
                return CommandResult(CommandType.ALARM, "Opening alarm settings.")
            } catch (e: Exception) {
                return CommandResult(CommandType.CHAT, "Alarm app not found on this device.")
            }
        }
        if (lowerCommand.contains("system info") || lowerCommand.contains("battery")) {
            return CommandResult(CommandType.SYSTEM_INFO, "All systems nominal. Battery levels are sufficient.")
        }

        // Fallback to Gemini AI for chat/general info
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isNullOrBlank()) {
                    return@withContext CommandResult(
                        CommandType.CHAT,
                        "API key is not configured. Please set GEMINI_API_KEY in the AI Studio Secrets panel."
                    )
                }
                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = command)))),
                    systemInstruction = Content(parts = listOf(Part(text = "You are JARVIS, an advanced, highly intelligent AI assistant. Keep responses witty, confident, fast, and concise (under 2 sentences if possible). Be slightly playful but extremely helpful.")))
                )
                val response = RetrofitClient.service.generateContent(apiKey, request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "I am having trouble processing that request at this moment."
                CommandResult(CommandType.CHAT, text)
            } catch (e: Exception) {
                CommandResult(CommandType.CHAT, "Communications disrupted: ${e.localizedMessage ?: "Unable to connect to AI engine."}")
            }
        }
    }

    private fun launchApp(packageName: String) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "App not installed on device", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open application: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
