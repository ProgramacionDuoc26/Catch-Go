package cl.catchgo.app.data.remote.dto

import cl.catchgo.app.domain.model.JobCategory
import cl.catchgo.app.domain.model.JobOffer
import kotlinx.serialization.Serializable

@Serializable
data class JobOfferDto(
    val id: Long,
    val titulo: String? = null,
    val descripcion: String? = null,
    val ubicacion: String? = null,
    val remuneracion: Int? = null,
    val fechaInicio: String? = null,
    val fechaFin: String? = null,
    val estado: String? = null,
    val empresaId: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    fun toDomain(): JobOffer {
        val parts = ubicacion?.split(",")?.map { it.trim() } ?: emptyList()
        val comuna = parts.getOrElse(0) { "Sin especificar" }
        val region = parts.getOrElse(1) { "Metropolitana" }
        return JobOffer(
            id = id.toString(),
            title = titulo ?: "Sin título",
            company = if (!empresaId.isNullOrBlank()) "Empresa #$empresaId" else "Sin empresa",
            category = JobCategory.OTRO,
            region = region,
            comuna = comuna,
            jornada = "Por definir",
            scheduleText = buildSchedule(fechaInicio, fechaFin),
            salaryClp = remuneracion ?: 0,
            salaryUnit = "trabajo",
            description = descripcion ?: "",
            requirements = emptyList(),
            score = 0
        )
    }

    private fun buildSchedule(inicio: String?, fin: String?): String {
        if (inicio == null) return "A coordinar"
        return if (fin != null) "$inicio → $fin" else "Desde $inicio"
    }
}
