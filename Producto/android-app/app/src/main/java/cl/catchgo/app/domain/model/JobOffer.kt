package cl.catchgo.app.domain.model

data class JobOffer(
    val id: String,
    val title: String,
    val company: String,
    val category: JobCategory,
    val region: String,
    val comuna: String,
    val jornada: String,
    val scheduleText: String,
    val salaryClp: Int,
    val salaryUnit: String,
    val description: String,
    val requirements: List<String>,
    val score: Int,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val empresaId: String? = null,
    val distanceKm: Double? = null,
    val photoUrl: String? = null
)
