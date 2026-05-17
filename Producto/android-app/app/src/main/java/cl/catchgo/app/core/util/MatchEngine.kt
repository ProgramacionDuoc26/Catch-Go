package cl.catchgo.app.core.util

import cl.catchgo.app.data.remote.dto.JobOfferDto
import cl.catchgo.app.data.remote.dto.ProfileRemoteDto
import org.json.JSONObject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

object MatchEngine {

    data class MatchScore(
        val total: Int,
        val habilidades: Int,
        val experiencia: Int,
        val distancia: Int,
        val disponibilidad: Int,
        val calificacion: Int
    )

    private val charactMap = mapOf(
        "Responsable" to "Responsabilidad",
        "Rápido" to "Rapidez",
        "Líder" to "Liderazgo",
        "Comunicativo" to "Comunicación",
        "Analítico" to "Resolución de problemas"
    )

    fun calculate(
        workerProfile: ProfileRemoteDto,
        companyProfile: ProfileRemoteDto,
        jobOffer: JobOfferDto?
    ): MatchScore {
        val wSkills = parseSkills(workerProfile.skills)
        val cSkills = parseSkills(companyProfile.skills)

        val habilidades = calcHabilidades(wSkills, cSkills)
        val experiencia = calcExperiencia(workerProfile.description)
        val distancia = calcDistancia(
            workerProfile.latitude, workerProfile.longitude,
            jobOffer?.latitude ?: companyProfile.latitude,
            jobOffer?.longitude ?: companyProfile.longitude
        )
        val disponibilidad = calcDisponibilidad(wSkills, jobOffer)
        val calificacion = calcCalificacion(workerProfile.rating)

        val total = min(100, habilidades + experiencia + distancia + disponibilidad + calificacion)
        return MatchScore(total, habilidades, experiencia, distancia, disponibilidad, calificacion)
    }

    private fun parseSkills(json: String?): JSONObject {
        if (json.isNullOrBlank()) return JSONObject()
        return runCatching { JSONObject(json) }.getOrElse { JSONObject() }
    }

    private fun calcHabilidades(wSkills: JSONObject, cSkills: JSONObject): Int {
        var score = 0
        val wCaract = wSkills.optString("caracteristica", "")
        val cHabil = cSkills.optString("habilidadValorada", "")
        if (wCaract.isNotEmpty() && cHabil.isNotEmpty()) {
            score += if (charactMap[wCaract] == cHabil || wCaract == cHabil) 20 else 10
        }
        val wAmbiente = wSkills.optString("ambiente", "")
        val cRitmo = cSkills.optString("ritmo", "")
        if (wAmbiente.isNotEmpty() && cRitmo.isNotEmpty()) {
            score += when {
                wAmbiente == cRitmo -> 20
                (wAmbiente == "Alta presión" && cRitmo == "Muy dinámico") ||
                        (wAmbiente == "Trabajo en equipo" && cRitmo == "Estructurado") -> 15
                else -> 5
            }
        } else {
            val habilidades = wSkills.optJSONArray("habilidades")
            if (habilidades != null && habilidades.length() > 0) score += 10
        }
        return min(40, score)
    }

    private fun calcExperiencia(description: String?): Int {
        val desc = description?.lowercase() ?: return 0
        var score = 0
        if (desc.length > 10) score += 10
        if (desc.contains("experiencia") || desc.contains("años") || desc.contains("trabajé")) score += 10
        return score
    }

    private fun calcDistancia(
        lat1: Double?, lon1: Double?,
        lat2: Double?, lon2: Double?
    ): Int {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return 10
        val dist = haversineKm(lat1, lon1, lat2, lon2)
        return when {
            dist <= 10.0 -> 20
            dist <= 25.0 -> 10
            else -> 5
        }
    }

    private fun calcDisponibilidad(wSkills: JSONObject, jobOffer: JobOfferDto?): Int {
        val pref = wSkills.optString("preferencia", "")
        if (pref.isEmpty()) return 10
        val jobText = ((jobOffer?.titulo ?: "") + " " + (jobOffer?.descripcion ?: "")).lowercase()
        return if (jobText.contains(pref.lowercase())) 20 else 15
    }

    private fun calcCalificacion(rating: Double?): Int {
        return when {
            rating == null || rating == 0.0 -> 0
            rating >= 4.8 -> 10
            rating >= 4.5 -> 7
            rating >= 4.0 -> 4
            else -> 0
        }
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
