package cl.catchgo.app.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalhostMappingInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.isSuccessful) {
            val body = response.body
            if (body != null) {
                val contentType = body.contentType()
                val bodyString = body.string()
                val mappedBodyString = bodyString
                    .replace("http://localhost", "http://10.0.2.2")
                    .replace("https://localhost", "https://10.0.2.2")
                    .replace("http://127.0.0.1", "http://10.0.2.2")
                    .replace("https://127.0.0.1", "https://10.0.2.2")
                val newBody = mappedBodyString.toResponseBody(contentType)
                return response.newBuilder().body(newBody).build()
            }
        }
        return response
    }
}
