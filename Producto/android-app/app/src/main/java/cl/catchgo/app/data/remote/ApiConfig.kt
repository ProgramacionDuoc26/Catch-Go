package cl.catchgo.app.data.remote

object ApiConfig {
    private const val HOST = "10.65.150.101"

    const val AUTH_URL = "http://$HOST:8081/"
    const val JOBS_URL = "http://$HOST:8083/"
    const val PROFILE_URL = "http://$HOST:8082/"

    const val USE_MOCK_AUTH = false
    const val USE_MOCK_JOBS = false
    const val USE_MOCK_APPLICATIONS = false
}
