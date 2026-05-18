package cl.catchgo.app.data.remote

object ApiConfig {
    // Para emulador Android estándar, usa "10.0.2.2" para apuntar al localhost de la máquina host.
    // Para dispositivo físico Android, usa la IP local de tu computador en la misma red Wi-Fi (ej: "192.168.1.XX").
    private const val HOST = "10.0.2.2"

    // --- OPCIÓN A: Conexión Integrada vía API Gateway (Comentada debido a restricción de red en Docker local) ---
    /*
    private const val GATEWAY_URL = "http://$HOST:8080/"
    const val AUTH_URL = GATEWAY_URL
    const val JOBS_URL = GATEWAY_URL
    const val PROFILE_URL = GATEWAY_URL
    const val MATCHING_URL = GATEWAY_URL
    */

    // --- OPCIÓN B: Conexión Directa a cada Microservicio (Activa - Igual que el Frontend Web) ---
    const val AUTH_URL = "http://$HOST:8081/"
    const val JOBS_URL = "http://$HOST:8083/"
    const val PROFILE_URL = "http://$HOST:8082/"
    const val MATCHING_URL = "http://$HOST:8084/"
    const val NOTIFICATIONS_WS_URL = "ws://$HOST:8088/ws-notifications/websocket"

    const val USE_MOCK_AUTH = false
    const val USE_MOCK_JOBS = false
    const val USE_MOCK_APPLICATIONS = false
}
