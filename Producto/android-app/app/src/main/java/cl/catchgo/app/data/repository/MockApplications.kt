package cl.catchgo.app.data.repository

import cl.catchgo.app.domain.model.ApplicationStatus
import cl.catchgo.app.domain.model.JobApplication
import java.time.Instant
import java.time.temporal.ChronoUnit

internal object MockApplications {
    val preseed: List<JobApplication> = listOf(
        JobApplication(
            id = "a-001",
            offerId = "j-003",
            offerTitle = "Conserje turno noche",
            company = "Hotel Sheraton",
            comuna = "Las Condes",
            message = "Tengo experiencia previa en hoteles boutique.",
            status = ApplicationStatus.ACCEPTED,
            createdAtIso = Instant.now().minus(2, ChronoUnit.DAYS).toString()
        ),
        JobApplication(
            id = "a-002",
            offerId = "j-007",
            offerTitle = "Temporero vendimia",
            company = "Concha y Toro",
            comuna = "Pirque",
            message = null,
            status = ApplicationStatus.PENDING,
            createdAtIso = Instant.now().minus(5, ChronoUnit.HOURS).toString()
        ),
        JobApplication(
            id = "a-003",
            offerId = "j-010",
            offerTitle = "Acomodador de eventos",
            company = "Estadio Nacional",
            comuna = "Ñuñoa",
            message = "Disponible este fin de semana.",
            status = ApplicationStatus.REJECTED,
            createdAtIso = Instant.now().minus(7, ChronoUnit.DAYS).toString()
        )
    )
}
