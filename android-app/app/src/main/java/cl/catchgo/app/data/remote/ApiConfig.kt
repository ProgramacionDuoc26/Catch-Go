package cl.catchgo.app.data.remote

object ApiConfig {
    const val BASE_URL = "http://10.0.2.2:8080/"

    // Mock: backend todavía no expone POST /auth/login. Bajar a false cuando esté listo.
    const val USE_MOCK_AUTH = true
}
