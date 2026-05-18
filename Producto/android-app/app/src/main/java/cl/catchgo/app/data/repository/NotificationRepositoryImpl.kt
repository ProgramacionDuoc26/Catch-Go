package cl.catchgo.app.data.repository

import android.util.Log
import cl.catchgo.app.data.remote.ApiConfig
import cl.catchgo.app.data.remote.dto.NotificationDto
import cl.catchgo.app.domain.model.AppNotification
import cl.catchgo.app.domain.repository.NotificationRepository
import cl.catchgo.app.util.NotificationSoundPlayer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.headers.StompSubscribeHeaders
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json
) : NotificationRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var stompSession: StompSession? = null
    private var connectionJob: Job? = null

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    override val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    override suspend fun connect(userId: String) {
        if (connectionJob?.isActive == true) return

        connectionJob = scope.launch {
            try {
                Log.d("NotificationRepository", "Connecting to STOMP WebSocket: \${ApiConfig.NOTIFICATIONS_WS_URL}")
                
                // Initialize WebSocket client using OkHttp
                val wsClient = OkHttpWebSocketClient(okHttpClient)
                val stompClient = StompClient(wsClient)

                // Connect to the WebSocket endpoint
                val session = stompClient.connect(ApiConfig.NOTIFICATIONS_WS_URL)
                stompSession = session
                
                _isConnected.value = true
                Log.d("NotificationRepository", "STOMP Connected successfully")

                // Subscribe to user specific topic using StompSubscribeHeaders
                val topic = "/topic/user/$userId"
                val subscription = session.subscribe(StompSubscribeHeaders(destination = topic))
                Log.d("NotificationRepository", "Subscribed to topic: $topic")
                
                subscription.collect { message ->
                    val bodyText = message.bodyAsText
                    Log.d("NotificationRepository", "Received raw message: $bodyText")
                    val dto = json.decodeFromString<NotificationDto>(bodyText)
                    val appNotif = AppNotification(
                        id = dto.id ?: UUID.randomUUID().toString(),
                        title = dto.title,
                        message = dto.message,
                        type = dto.type,
                        timestamp = System.currentTimeMillis(),
                        isRead = false
                    )
                    
                    // Add to list, newest first
                    _notifications.update { current ->
                        listOf(appNotif) + current
                    }

                    // Play sound based on notification type
                    when (appNotif.type.lowercase()) {
                        "success" -> NotificationSoundPlayer.playSuccess()
                        "error" -> NotificationSoundPlayer.playError()
                        "warning" -> NotificationSoundPlayer.playWarning()
                        else -> NotificationSoundPlayer.playInfo()
                    }
                    Log.d("NotificationRepository", "Processed notification: ${appNotif.title}")
                }

            } catch (e: Exception) {
                Log.e("NotificationRepository", "STOMP Connection failed", e)
                _isConnected.value = false
            }
        }
    }

    override suspend fun disconnect() {
        connectionJob?.cancel()
        connectionJob = null
        try {
            stompSession?.disconnect()
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error disconnecting", e)
        } finally {
            stompSession = null
            _isConnected.value = false
        }
    }

    override fun markAsRead(id: String) {
        _notifications.update { current ->
            current.map { if (it.id == id) it.copy(isRead = true) else it }
        }
    }

    override fun clearAll() {
        _notifications.value = emptyList()
    }
}
