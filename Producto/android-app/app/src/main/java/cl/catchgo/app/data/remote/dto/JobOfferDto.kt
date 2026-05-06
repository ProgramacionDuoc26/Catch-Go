package cl.catchgo.app.data.remote.dto

import cl.catchgo.app.domain.model.JobCategory
import cl.catchgo.app.domain.model.JobOffer
import kotlinx.serialization.Serializable

@Serializable
data class JobOfferDto(
    val id: String,
    val title: String,
    val company: String,
    val category: String,
    val region: String,
    val comuna: String,
    val jornada: String,
    val scheduleText: String,
    val salaryClp: Int,
    val salaryUnit: String,
    val description: String,
    val requirements: List<String> = emptyList(),
    val score: Int = 0
) {
    fun toDomain(): JobOffer = JobOffer(
        id = id,
        title = title,
        company = company,
        category = runCatching { JobCategory.valueOf(category.uppercase()) }
            .getOrDefault(JobCategory.OTRO),
        region = region,
        comuna = comuna,
        jornada = jornada,
        scheduleText = scheduleText,
        salaryClp = salaryClp,
        salaryUnit = salaryUnit,
        description = description,
        requirements = requirements,
        score = score
    )
}
