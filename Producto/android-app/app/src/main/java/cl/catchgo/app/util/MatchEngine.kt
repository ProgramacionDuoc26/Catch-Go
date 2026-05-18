package cl.catchgo.app.util

import cl.catchgo.app.data.remote.dto.ProfileRemoteDto
import cl.catchgo.app.domain.model.JobOffer
import org.json.JSONObject
import kotlin.math.*

object MatchEngine {

    fun calculateDistance(lat1: Double?, lon1: Double?, lat2: Double?, lon2: Double?): Double {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return -1.0
        val r = 6371.0 // km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        return r * c
    }

    fun calculate(
        worker: ProfileRemoteDto?,
        company: ProfileRemoteDto?,
        offer: JobOffer,
        plan: String = "TRIAL"
    ): Int {
        val isPremium = plan == "PREMIUM" || plan == "ENTERPRISE"
        
        var habilidadesScore = 0
        var experienciaScore = 0
        var distanciaScore = 0
        var disponibilidadScore = 0
        var calificacionScore = 0

        // Parse skills safely
        val wSkills = try {
            JSONObject(worker?.skills ?: "{}")
        } catch (e: Exception) {
            JSONObject()
        }
        val cSkills = try {
            JSONObject(company?.skills ?: "{}")
        } catch (e: Exception) {
            JSONObject()
        }

        // 1. Habilidades (40%)
        val wCaract = wSkills.optString("caracteristica", "")
        val cHabil = cSkills.optString("habilidadValorada", "")
        
        val charactMap = mapOf(
            "Responsable" to "Responsabilidad",
            "Rápido" to "Rapidez",
            "Líder" to "Liderazgo",
            "Comunicativo" to "Comunicación",
            "Analítico" to "Resolución de problemas"
        )

        if (wCaract.isNotBlank() && cHabil.isNotBlank()) {
            if (charactMap[wCaract] == cHabil || wCaract == cHabil) {
                habilidadesScore += 20
            } else {
                habilidadesScore += 10
            }
        }

        val wAmbiente = wSkills.optString("ambiente", "")
        val cRitmo = cSkills.optString("ritmo", "")

        if (wAmbiente.isNotBlank() && cRitmo.isNotBlank()) {
            if (wAmbiente == cRitmo) {
                habilidadesScore += 20
            } else if (
                (wAmbiente == "Alta presión" && cRitmo == "Muy dinámico") ||
                (wAmbiente == "Trabajo en equipo" && cRitmo == "Estructurado")
            ) {
                habilidadesScore += 15
            } else {
                habilidadesScore += 5
            }
        } else {
            val habArray = wSkills.optJSONArray("habilidades")
            if (habArray != null && habArray.length() > 0) {
                habilidadesScore += 10
            }
        }

        habilidadesScore = minOf(40, habilidadesScore)

        // 2. Experiencia (20%)
        val desc = (worker?.description ?: "").lowercase()
        if (desc.length > 10) experienciaScore += 10
        if (desc.contains("experiencia") || desc.contains("años") || desc.contains("trabajé") || desc.contains("trabaje")) {
            experienciaScore += 10
        }

        // 3. Distancia (20%)
        val targetLat = offer.latitude ?: company?.latitude
        val targetLng = offer.longitude ?: company?.longitude
        
        val dist = calculateDistance(worker?.latitude, worker?.longitude, targetLat, targetLng)
        if (dist < 0.0) {
            distanciaScore = 10
        } else if (isPremium) {
            distanciaScore = when {
                dist <= 5.0 -> 20
                dist <= 10.0 -> 15
                dist <= 15.0 -> 10
                else -> 5
            }
        } else {
            distanciaScore = if (dist <= 5.0) 20 else 0
        }

        // 4. Disponibilidad (20%)
        val wPref = wSkills.optString("preferencia", "")
        val jobTitleDesc = (offer.title + " " + offer.description).lowercase()

        if (wPref.isNotBlank()) {
            if (jobTitleDesc.contains(wPref.lowercase())) {
                disponibilidadScore = 20
            } else {
                disponibilidadScore = 15
            }
        } else {
            disponibilidadScore = 10
        }

        // 5. Calificación (Bonus +10)
        val workerRating = worker?.rating ?: 0.0
        calificacionScore = when {
            workerRating >= 4.8 -> 10
            workerRating >= 4.5 -> 7
            workerRating >= 4.0 -> 4
            else -> 0
        }

        var total = habilidadesScore + experienciaScore + distanciaScore + disponibilidadScore + calificacionScore

        if (isPremium) {
            total = (total * 1.20).roundToInt()
        }

        return minOf(100, maxOf(0, total))
    }

    fun calculateProfileCompletion(profile: ProfileRemoteDto?): Int {
        if (profile == null) return 0
        
        var score = 0
        var totalWeight = 0

        fun addScore(condition: Boolean, weight: Int) {
            totalWeight += weight
            if (condition) score += weight
        }

        // Datos Básicos
        addScore(!profile.name.isNullOrBlank(), 10)
        addScore(!profile.email.isNullOrBlank(), 10)
        
        val phone = profile.phone ?: ""
        val hasPhone = phone.isNotBlank() && phone != "+56 "
        addScore(hasPhone, 10)
        
        // Archivos
        addScore(!profile.photoUrl.isNullOrBlank(), 10)
        addScore(!profile.cvUrl.isNullOrBlank(), 15)

        // Ubicación
        addScore(profile.latitude != null && profile.longitude != null, 10)

        // Datos Bancarios (Todos o nada)
        val hasBank = !profile.bankName.isNullOrBlank() && 
                      !profile.accountType.isNullOrBlank() && 
                      !profile.accountNumber.isNullOrBlank()
        addScore(hasBank, 15)

        // Skills / Preferencias
        var hasSkills = false
        try {
            val skillsStr = profile.skills
            if (!skillsStr.isNullOrBlank()) {
                val parsed = JSONObject(skillsStr)
                val habArray = parsed.optJSONArray("habilidades")
                val hasHabilidades = habArray != null && habArray.length() > 0
                
                if (hasHabilidades ||
                    !parsed.optString("ambiente").isNullOrBlank() ||
                    !parsed.optString("caracteristica").isNullOrBlank() ||
                    !parsed.optString("preferencia").isNullOrBlank() ||
                    !parsed.optString("giro").isNullOrBlank() ||
                    !parsed.optString("tipoTrabajador").isNullOrBlank() ||
                    !parsed.optString("habilidadValorada").isNullOrBlank() ||
                    !parsed.optString("ritmo").isNullOrBlank()
                ) {
                    hasSkills = true
                }
            }
        } catch (e: Exception) {
            // ignore
        }
        addScore(hasSkills, 20)

        if (totalWeight == 0) return 0
        return minOf(100, ((score.toDouble() / totalWeight.toDouble()) * 100.0).roundToInt())
    }
}
