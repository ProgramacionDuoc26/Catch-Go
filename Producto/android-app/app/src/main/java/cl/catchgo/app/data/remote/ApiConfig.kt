package cl.catchgo.app.data.remote

object ApiConfig {
    const val BASE_URL = "http://10.0.2.2:8080/"

    // Mock: backend todavía no expone /auth/login ni /auth/register reales (solo CRUDs triviales).
    // Bajar a false cuando los endpoints estén implementados.
    const val USE_MOCK_AUTH = true

    // Mock: backend /jobs solo retorna DTO trivial (id, name). Bajar cuando JobOfferDto real exista.
    const val USE_MOCK_JOBS = true

    // Mock: backend no expone /postulaciones todavía. Bajar cuando exista el contrato real.
    const val USE_MOCK_APPLICATIONS = true
}
