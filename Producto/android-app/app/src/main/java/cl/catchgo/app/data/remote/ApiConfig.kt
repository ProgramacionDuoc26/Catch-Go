package cl.catchgo.app.data.remote

object ApiConfig {
    // Host de producción en Railway
    const val HOST = "api-gateway2-catch-go.up.railway.app"

    // --- OPCIÓN A: Conexión Integrada vía API Gateway (Activa para Railway) ---
    private const val GATEWAY_URL = "https://$HOST/"
    const val AUTH_URL = GATEWAY_URL
    const val JOBS_URL = GATEWAY_URL
    const val PROFILE_URL = GATEWAY_URL
    const val MATCHING_URL = GATEWAY_URL

    // --- OPCIÓN B: Conexión Directa a cada Microservicio (Inactiva) ---
    /*
    const val AUTH_URL = "http://$HOST:8081/"
    const val JOBS_URL = "http://$HOST:8083/"
    const val PROFILE_URL = "http://$HOST:8082/"
    const val MATCHING_URL = "http://$HOST:8084/"
    */
    const val NOTIFICATIONS_WS_URL = "wss://$HOST/ws-notifications/websocket"

    const val USE_MOCK_AUTH = false
    const val USE_MOCK_JOBS = false
    const val USE_MOCK_APPLICATIONS = false
}

