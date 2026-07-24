package com.example.ui.speech

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Base64
import android.util.Log
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineExceptionHandler
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

class ElevenLabsVoiceAgentManager(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .pingInterval(10, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val audioLock = Any()
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null

    private var isRecording = false
    private var isPlaying = false

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("ElevenLabsVoice", "Coroutine crash caught in scope: ${exception.message}", exception)
        _state.value = "error"
        _error.value = exception.localizedMessage ?: exception.message ?: "Internal Voicelink Error"
    }

    private val scope = CoroutineScope(Dispatchers.IO + kotlinx.coroutines.SupervisorJob() + exceptionHandler)
    private var recordingJob: Job? = null

    private var currentUserName: String = "Learner"
    private var currentStreak: String = "1"
    private var reconnectCount = 0
    private var lastMessageTime = 0L
    private var reconnectJob: Job? = null
    private var connectionWatchdogJob: Job? = null
    private var playbackJob: Job? = null
    private var audioPlaybackChannel = Channel<ByteArray>(Channel.UNLIMITED)

    private val _state = MutableStateFlow("idle") // "idle", "connecting", "ringing", "active", "ended", "error", "sandbox"
    val state: StateFlow<String> = _state

    private val _userTranscript = MutableStateFlow("")
    val userTranscript: StateFlow<String> = _userTranscript

    private val _agentTranscript = MutableStateFlow("")
    val agentTranscript: StateFlow<String> = _agentTranscript

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var isMuted = false

    fun startSimulatedCall() {
        _state.value = "sandbox"
        _userTranscript.value = ""
        _agentTranscript.value = "Vocal carrier link is offline. Starting Local AI Sandbox. Hello, I am your EcoMind AI Coach! Tap a query below to interact with me."
        _error.value = null
        isMuted = false
    }

    fun speakSimulated(text: String) {
        // No-op (TTS disabled as requested)
    }

    fun updateSandboxTranscripts(user: String, agent: String) {
        _userTranscript.value = user
        _agentTranscript.value = agent
    }

    fun updateSandboxState(newState: String) {
        _state.value = newState
    }

    @SuppressLint("MissingPermission")
    fun startCall(userName: String = "Learner", streak: String = "1") {
        if (_state.value == "connecting" || _state.value == "active") return

        currentUserName = userName
        currentStreak = streak

        _state.value = "connecting"
        _userTranscript.value = ""
        _agentTranscript.value = "Establishing real-time vocal link..."
        _error.value = null
        isMuted = false

        // 1. Initialize Audio Track playback
        initAudioTrack()

        // Setup clear Channel and start the playback worker
        audioPlaybackChannel = Channel(Channel.UNLIMITED)
        isPlaying = true
        startPlaybackQueue()

        // 2. Initialize WebSocket to ElevenLabs Conversation Agent ID
        val agentId = "agent_6501ks7smcbcez097ey5eypbvjgv"
        val apiKey = "sk_0fdb9aef124b053f160c596c7d580939ec5662319303797d"
        val request = Request.Builder()
            .url("wss://api.elevenlabs.io/v1/convai/conversation?agent_id=$agentId")
            .addHeader("xi-api-key", apiKey)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                if (_state.value != "connecting") {
                    try {
                        webSocket.close(1000, "Call cancelled")
                    } catch (e: Exception) {}
                    return
                }
                _state.value = "active"
                _agentTranscript.value = "Connected to EcoMind AI Agent. Listening..."
                Log.d("ElevenLabsVoice", "ElevenLabs Voice Web Socket opened.")

                reconnectCount = 0
                lastMessageTime = System.currentTimeMillis()
                startConnectionWatchdog()

                // Send conversation_initiation_client_data first with dynamic variables
                try {
                    val streakInt = streak.toIntOrNull() ?: 1
                    val vars = JSONObject().apply {
                        put("user_name", userName)
                        put("streak", streakInt)
                    }
                    val messageObj = JSONObject().apply {
                        put("type", "conversation_initiation_client_data")
                        put("dynamic_variables", vars)
                    }
                    Log.d("ElevenLabsVoice", "Sending dynamic variable handshake (conversation_initiation_client_data): $messageObj")
                    webSocket.send(messageObj.toString())
                } catch (e: Exception) {
                    Log.e("ElevenLabsVoice", "Failed to send conversation initiation client data handshake", e)
                }

                // Start recording raw chunks and sending
                startRecording()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                if (_state.value != "active") return
                lastMessageTime = System.currentTimeMillis()
                parseServerMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("ElevenLabsVoice", "WebSocket carrier connection failed", t)
                if (_state.value == "ended" || _state.value == "idle") {
                    return
                }
                _state.value = "error"
                _error.value = t.message ?: "Handshake disrupted or network timeout."
                _agentTranscript.value = "Voicelink Carrier Disturbed: ${_error.value}"
                stopCall()
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("ElevenLabsVoice", "Connection closing: code=$code, reason=$reason")
                if (_state.value == "ended" || _state.value == "idle") {
                    return
                }
                if (code != 1000) {
                    _state.value = "error"
                    _error.value = if (reason.isNotEmpty()) reason else "Connection closed by ElevenLabs (code $code)."
                    _agentTranscript.value = _error.value ?: ""
                } else {
                    _state.value = "ended"
                }
                stopCall()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("ElevenLabsVoice", "Connection closed: code=$code, reason=$reason")
                if (_state.value == "ended" || _state.value == "idle") {
                    return
                }
                if (code != 1000) {
                    _state.value = "error"
                } else {
                    _state.value = "ended"
                }
            }
        })
    }

    private fun parseServerMessage(rawText: String) {
        try {
            val json = JSONObject(rawText)
            
            // Check for Audio chunk payload
            var base64Audio: String? = null
            if (json.has("type") && json.getString("type") == "audio") {
                val audioEvent = json.optJSONObject("audio_event")
                base64Audio = audioEvent?.optString("audio_base_64") ?: audioEvent?.optString("audio")
            } else if (json.has("audio_event")) {
                val audioEvent = json.optJSONObject("audio_event")
                base64Audio = audioEvent?.optString("audio_base_64") ?: audioEvent?.optString("audio")
            } else if (json.has("audio")) {
                base64Audio = json.getString("audio")
            }

            if (!base64Audio.isNullOrEmpty()) {
                val audioBytes = Base64.decode(base64Audio, Base64.DEFAULT)
                audioPlaybackChannel.trySend(audioBytes)
            }

            // Check for dialogue transcript, metadata, and interruption updates
            if (json.has("type")) {
                val type = json.getString("type")
                when (type) {
                    "conversation_initiation_metadata" -> {
                        val metaEvent = json.optJSONObject("conversation_initiation_metadata_event")
                        val outputFormat = metaEvent?.optString("agent_output_audio_format") ?: "pcm_16000"
                        val sampleRate = when (outputFormat) {
                            "pcm_44100" -> 44100
                            "pcm_24000" -> 24000
                            "pcm_22050" -> 22050
                            "pcm_16000" -> 16000
                            "pcm_8000" -> 8000
                            else -> {
                                val digits = outputFormat.filter { it.isDigit() }
                                digits.toIntOrNull() ?: 16000
                            }
                        }
                        Log.d("ElevenLabsVoice", "Metadata received. Reinitializing AudioTrack to $sampleRate Hz (format: $outputFormat)")
                        initAudioTrack(sampleRate)
                    }
                    "interruption" -> {
                        handleInterruption()
                    }
                    "user_transcript" -> {
                        val transEvent = json.optJSONObject("user_transcript_event")
                        val transcript = transEvent?.optString("transcript") ?: ""
                        if (transcript.isNotEmpty()) {
                            _userTranscript.value = transcript
                        }
                    }
                    "agent_response" -> {
                        val respEvent = json.optJSONObject("agent_response_event")
                        val agentResponse = respEvent?.optString("agent_response") ?: ""
                        if (agentResponse.isNotEmpty()) {
                            _agentTranscript.value = agentResponse
                        }
                    }
                    "ping" -> {
                        val eventId = json.optInt("event_id")
                        val pong = JSONObject().apply {
                            put("type", "pong")
                            put("event_id", eventId)
                        }
                        webSocket?.send(pong.toString())
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ElevenLabsVoice", "Error processing audio/transcript stream message: ${e.message}")
        }
    }

    private fun clearAudioQueue() {
        while (true) {
            val polled = audioPlaybackChannel.tryReceive()
            if (polled.isFailure || polled.isClosed) break
        }
    }

    private fun handleInterruption() {
        Log.d("ElevenLabsVoice", "Interruption received from ElevenLabs. Flushing audio buffers.")
        clearAudioQueue()
        synchronized(audioLock) {
            try {
                audioTrack?.flush()
                audioTrack?.stop()
                audioTrack?.play()
            } catch (e: Exception) {
                Log.e("ElevenLabsVoice", "Error flushing AudioTrack on interruption: ${e.message}")
            }
        }
    }

    private fun initAudioTrack(sampleRate: Int = 16000) {
        synchronized(audioLock) {
            try {
                audioTrack?.stop()
                audioTrack?.release()
                audioTrack = null
            } catch (e: Exception) {
                // ignore
            }

            try {
                val channelConfig = AudioFormat.CHANNEL_OUT_MONO
                val audioFormat = AudioFormat.ENCODING_PCM_16BIT
                val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
                val finalBufferSize = if (minBufferSize <= 0) 8192 else minBufferSize * 2

                // Route output to speaker using USAGE_MEDIA (STREAM_MUSIC)
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()

                val format = AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelConfig)
                    .setEncoding(audioFormat)
                    .build()

                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(format)
                    .setBufferSizeInBytes(finalBufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                requestAudioFocus()
                forceSpeakerphone()

                audioTrack?.play()
                isPlaying = true
                Log.d("ElevenLabsVoice", "Audio playback pipeline initialized successfully via USAGE_MEDIA at $sampleRate Hz. MinBuffer: $minBufferSize, FinalBuffer: $finalBufferSize")
            } catch (e: Exception) {
                Log.e("ElevenLabsVoice", "AudioPlayback: AudioTrack initialization or play failed: ${e.message}", e)
                _error.value = "Vocal connection warning: local audio hardware unavailable."
                isPlaying = false
            }
        }
    }

    private fun requestAudioFocus() {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val focusRequest = android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setOnAudioFocusChangeListener { focusChange ->
                        Log.d("ElevenLabsVoice", "Audio focus change: $focusChange")
                    }
                    .build()
                audioManager.requestAudioFocus(focusRequest)
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(
                    { focusChange -> Log.d("ElevenLabsVoice", "Audio focus change: $focusChange") },
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                )
            }
        } catch (e: Exception) {
            Log.e("ElevenLabsVoice", "Failed to request audio focus: ${e.message}")
        }
    }

    private fun forceSpeakerphone() {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.mode = AudioManager.MODE_NORMAL
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val devices = audioManager.availableCommunicationDevices
                val speakerDevice = devices.find { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
                if (speakerDevice != null) {
                    val success = audioManager.setCommunicationDevice(speakerDevice)
                    Log.d("ElevenLabsVoice", "Forced speaker routing via setCommunicationDevice (Android 12+): $success")
                } else {
                    Log.w("ElevenLabsVoice", "Built-in speaker device for voice communication routing not found!")
                    @Suppress("DEPRECATION")
                    audioManager.isSpeakerphoneOn = true
                }
            } else {
                @Suppress("DEPRECATION")
                audioManager.isSpeakerphoneOn = true
                Log.d("ElevenLabsVoice", "Forced speaker routing (legacy): MODE_NORMAL and isSpeakerphoneOn=true")
            }
        } catch (e: Exception) {
            Log.e("ElevenLabsVoice", "Failed to force speakerphone: ${e.message}", e)
        }
    }

    private fun resetSpeakerphone() {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.mode = AudioManager.MODE_NORMAL
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                audioManager.clearCommunicationDevice()
                Log.d("ElevenLabsVoice", "Cleared communication device setting.")
            } else {
                @Suppress("DEPRECATION")
                audioManager.isSpeakerphoneOn = false
                Log.d("ElevenLabsVoice", "Reset speakerphone routing to false (legacy).")
            }
        } catch (e: Exception) {
            Log.e("ElevenLabsVoice", "Failed to reset speakerphone: ${e.message}", e)
        }
    }

    private fun startPlaybackQueue() {
        playbackJob?.cancel()
        playbackJob = scope.launch(Dispatchers.Default) {
            while (isPlaying) {
                try {
                    val audioBytes = audioPlaybackChannel.receive()
                    val currentTrack = synchronized(audioLock) {
                        if (isPlaying) audioTrack else null
                    }
                    if (currentTrack != null && currentTrack.state == AudioTrack.STATE_INITIALIZED) {
                        try {
                            if (currentTrack.playState != AudioTrack.PLAYSTATE_PLAYING) {
                                currentTrack.play()
                            }
                        } catch (e: Exception) {
                            Log.e("ElevenLabsVoice", "Error starting/playing AudioTrack in channel loop: ${e.message}")
                        }
                        
                        val written = try {
                            currentTrack.write(audioBytes, 0, audioBytes.size)
                        } catch (e: Exception) {
                            -1
                        }
                        if (written < 0) {
                            Log.e("ElevenLabsVoice", "AudioTrack write failed with code: $written")
                        }
                    }
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    private fun startConnectionWatchdog() {
        connectionWatchdogJob?.cancel()
        lastMessageTime = System.currentTimeMillis()
        connectionWatchdogJob = scope.launch {
            try {
                while (_state.value == "active" || _state.value == "connecting") {
                    delay(5000)
                    val elapsedSinceLastMessage = System.currentTimeMillis() - lastMessageTime
                    if (_state.value == "active" && elapsedSinceLastMessage > 25000) {
                        Log.w("ElevenLabsVoice", "Packet stream paused. Reconnecting gracefully...")
                        reconnectGracefully()
                        break
                    }
                }
            } catch (e: Exception) {
                Log.e("ElevenLabsVoice", "Error in connection watchdog loop", e)
            }
        }
    }

    private fun reconnectGracefully() {
        if (_state.value == "idle" || _state.value == "ended") return

        Log.d("ElevenLabsVoice", "Graceful reconnect initiated.")
        _agentTranscript.value = "Reconnecting vocal carrier stream..."
        
        val savedUserName = currentUserName
        val savedStreak = currentStreak

        stopCall(isReconnecting = true)

        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            try {
                delay(2000)
                if (reconnectCount < 5) {
                    reconnectCount++
                    Log.d("ElevenLabsVoice", "Reconnection attempt $reconnectCount of 5")
                    startCall(savedUserName, savedStreak)
                } else {
                    Log.e("ElevenLabsVoice", "Max reconnection attempts reached.")
                    _state.value = "error"
                    _error.value = "Voice link carrier lost. Please try calling again."
                    _agentTranscript.value = "Silent stream timeout. Portal disconnected."
                }
            } catch (e: Exception) {
                Log.e("ElevenLabsVoice", "Error in reconnectJob", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startRecording() {
        synchronized(audioLock) {
            if (isRecording) return
            isRecording = true
        }

        val sampleRate = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        
        // Ensure buffer is large enough for stable reading, but small enough for low latency
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat).let { 
            if (it <= 0) 4096 else it * 2 
        }

        synchronized(audioLock) {
            if (!isRecording) return
            try {
                // First try VOICE_COMMUNICATION for built-in echo cancellation
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    minBufferSize
                )
                audioRecord?.startRecording()
            } catch (e: Exception) {
                Log.w("ElevenLabsVoice", "VOICE_COMMUNICATION recording source failed, trying default MIC source...", e)
                try {
                    audioRecord = AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        sampleRate,
                        channelConfig,
                        audioFormat,
                        minBufferSize
                    )
                    audioRecord?.startRecording()
                } catch (ex: Exception) {
                    Log.e("ElevenLabsVoice", "Both VOICE_COMMUNICATION and MIC recording sources failed: ${ex.message}", ex)
                    isRecording = false
                    return
                }
            }
        }

        recordingJob = scope.launch {
            try {
                // 16000Hz * 2 bytes/sample * 0.100s (100ms) = 3200 bytes
                // Smaller chunks (e.g. 40ms/1280 bytes) are better for latency if stable
                val chunkSize = 3200 
                val buffer = ByteArray(chunkSize)
                
                Log.d("ElevenLabsVoice", "Recording loop started with chunkSize=$chunkSize")

                while (isRecording) {
                    val currentRecord = synchronized(audioLock) {
                        if (isRecording) audioRecord else null
                    }
                    if (currentRecord != null && currentRecord.state == AudioRecord.STATE_INITIALIZED) {
                        if (currentRecord.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                            try {
                                currentRecord.startRecording()
                                Log.d("ElevenLabsVoice", "AudioRecord started recording")
                            } catch (e: Exception) {
                                Log.e("ElevenLabsVoice", "Failed to startRecording in loop: ${e.message}")
                            }
                        }
                        
                        val read = try {
                            currentRecord.read(buffer, 0, buffer.size)
                        } catch (e: Exception) {
                            Log.e("ElevenLabsVoice", "Error reading audio hardware: ${e.message}")
                            -1
                        }

                        if (read > 0 && isRecording) {
                            if (!isMuted) {
                                // Important: ElevenLabs Conversational AI expects raw PCM in user_audio_chunk
                                val base64Chunk = Base64.encodeToString(buffer, 0, read, Base64.NO_WRAP)
                                val message = JSONObject().apply {
                                    put("user_audio_chunk", base64Chunk)
                                }
                                
                                val messageStr = message.toString()
                                val sent = webSocket?.send(messageStr) ?: false
                                
                                if (!sent) {
                                    Log.w("ElevenLabsVoice", "WebSocket transmission failed")
                                }
                            }
                        } else if (read < 0) {
                            Log.e("ElevenLabsVoice", "AudioRecord read error: $read")
                            delay(100)
                        } else {
                            // read == 0, just wait for next cycle
                            delay(10)
                        }
                    } else {
                        Log.w("ElevenLabsVoice", "AudioRecord not initialized, waiting...")
                        delay(200)
                    }
                }
            } catch (e: Exception) {
                Log.e("ElevenLabsVoice", "Critical error in recording job: ${e.message}", e)
            } finally {
                Log.d("ElevenLabsVoice", "Recording loop exited")
            }
        }
    }

    fun stopCall(isReconnecting: Boolean = false) {
        if (!isReconnecting) {
            if (_state.value != "error" && _state.value != "sandbox" && _state.value != "sandbox_thinking") {
                _state.value = "ended"
            }
        } else {
            // Keep the state as connecting during reconnect to avoid triggering auto-close UI observer
            _state.value = "connecting"
        }
        synchronized(audioLock) {
            isRecording = false
            isPlaying = false
        }

        recordingJob?.cancel()
        recordingJob = null

        playbackJob?.cancel()
        playbackJob = null

        connectionWatchdogJob?.cancel()
        connectionWatchdogJob = null

        try {
            webSocket?.close(1000, "User close portal")
            webSocket = null
        } catch (e: Exception) {
            // ignore
        }

        synchronized(audioLock) {
            try {
                audioRecord?.stop()
                audioRecord?.release()
                audioRecord = null
            } catch (e: Exception) {
                // ignore
            }

            try {
                audioTrack?.stop()
                audioTrack?.release()
                audioTrack = null
            } catch (e: Exception) {
                // ignore
            }
        }

        resetSpeakerphone()
    }

    fun toggleMute() {
        isMuted = !isMuted
        if (isMuted) {
            _userTranscript.value = "Microphone muted"
        } else {
            _userTranscript.value = ""
        }
    }

    fun isMuted(): Boolean = isMuted

    fun destroy() {
        stopCall()
    }
}
