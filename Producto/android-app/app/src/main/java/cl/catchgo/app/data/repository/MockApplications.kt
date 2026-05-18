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
            rawStatus = "ACEPTADO",
            createdAtIso = Instant.now().minus(2, ChronoUnit.DAYS).toString(),
            photoUrl = "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=120&auto=format&fit=crop&q=60"
        ),
        JobApplication(
            id = "a-002",
            offerId = "j-007",
            offerTitle = "Temporero vendimia",
            company = "Concha y Toro",
            comuna = "Pirque",
            message = null,
            status = ApplicationStatus.PENDING,
            rawStatus = "PENDIENTE",
            createdAtIso = Instant.now().minus(5, ChronoUnit.HOURS).toString(),
            photoUrl = "https://images.unsplash.com/photo-1506306813292-23c241a3fa40?w=120&auto=format&fit=crop&q=60"
        ),
        JobApplication(
            id = "a-003",
            offerId = "j-010",
            offerTitle = "Acomodador de eventos",
            company = "Estadio Nacional",
            comuna = "Ñuñoa",
            message = "Disponible este fin de semana.",
            status = ApplicationStatus.REJECTED,
            rawStatus = "RECHAZADO",
            createdAtIso = Instant.now().minus(7, ChronoUnit.DAYS).toString(),
            photoUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=120&auto=format&fit=crop&q=60"
        )
    )
}
