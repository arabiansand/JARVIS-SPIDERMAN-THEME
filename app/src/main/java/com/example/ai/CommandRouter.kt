package com.example.ai

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.Settings
import android.view.KeyEvent
import android.media.AudioManager
import android.widget.Toast
import com.example.api.RetrofitClient
import com.example.api.GenerateContentRequest
import com.example.api.Content
import com.example.api.Part
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.BuildConfig

enum class CommandType {
    CHAT, APP_LAUNCH, ALARM, SYSTEM_INFO, MEDIA_CONTROL
}

data class CommandResult(val type: CommandType, val responseText: String)

class CommandRouter(private val context: Context) {

    suspend fun processCommand(command: String, base64Image: String? = null): CommandResult {
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
        if (lowerCommand.contains("set a timer") || lowerCommand.contains("set timer")) {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                val minRegex = Regex("([0-9]+)\\s*minute")
                val secRegex = Regex("([0-9]+)\\s*second")
                val minMatch = minRegex.find(lowerCommand)
                val secMatch = secRegex.find(lowerCommand)
                var totalSeconds = 0
                if (minMatch != null) totalSeconds += minMatch.groupValues[1].toInt() * 60
                if (secMatch != null) totalSeconds += secMatch.groupValues[1].toInt()
                if (totalSeconds == 0) totalSeconds = 60 // default
                putExtra(AlarmClock.EXTRA_LENGTH, totalSeconds)
            }
            try {
                context.startActivity(intent)
                return CommandResult(CommandType.ALARM, "Timer started.")
            } catch (e: Exception) {
                return CommandResult(CommandType.CHAT, "Timer app not found.")
            }
        }

        if (lowerCommand.contains("set an alarm") || lowerCommand.contains("set alarm") || lowerCommand.contains("wake me up at")) {
            val hourRegex = Regex("([0-9]{1,2})(:([0-9]{2}))?\\s*(am|pm)?")
            val match = hourRegex.find(lowerCommand.replace("set an alarm", "").replace("set alarm", "").replace("for", ""))
            
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                if (match != null) {
                    var hour = match.groupValues[1].toIntOrNull() ?: 8
                    val minute = match.groupValues[3].toIntOrNull() ?: 0
                    val ampm = match.groupValues[4]
                    if (ampm == "pm" && hour < 12) hour += 12
                    if (ampm == "am" && hour == 12) hour = 0
                    putExtra(AlarmClock.EXTRA_HOUR, hour)
                    putExtra(AlarmClock.EXTRA_MINUTES, minute)
                }
            }
            try {
                context.startActivity(intent)
                return CommandResult(CommandType.ALARM, "Alarm set.")
            } catch (e: Exception) {
                return CommandResult(CommandType.CHAT, "Alarm app not found on this device.")
            }
        }

        if (lowerCommand.contains("play music") || lowerCommand.contains("resume music")) {
            sendMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY)
            return CommandResult(CommandType.MEDIA_CONTROL, "Playing music.")
        }
        if (lowerCommand.contains("pause music") || lowerCommand.contains("stop music")) {
            sendMediaKey(KeyEvent.KEYCODE_MEDIA_PAUSE)
            return CommandResult(CommandType.MEDIA_CONTROL, "Music paused.")
        }
        if (lowerCommand.contains("next track") || lowerCommand.contains("skip song")) {
            sendMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
            return CommandResult(CommandType.MEDIA_CONTROL, "Skipping to next track.")
        }
        if (lowerCommand.contains("previous track") || lowerCommand.contains("last song")) {
            sendMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
            return CommandResult(CommandType.MEDIA_CONTROL, "Playing previous track.")
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

                val parts = mutableListOf<Part>()
                parts.add(Part(text = command))
                if (base64Image != null) {
                    parts.add(Part(inlineData = com.example.api.InlineData(mimeType = "image/jpeg", data = base64Image)))
                }

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = parts)),
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

    private fun sendMediaKey(keyCode: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }
}
