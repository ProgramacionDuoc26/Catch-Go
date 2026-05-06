package cl.catchgo.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateApplicationRequest(
    val offerId: String,
    val message: String? = null
)
