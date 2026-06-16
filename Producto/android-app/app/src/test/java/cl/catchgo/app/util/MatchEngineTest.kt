package cl.catchgo.app.util

import cl.catchgo.app.data.remote.dto.ProfileRemoteDto
import cl.catchgo.app.domain.model.JobCategory
import cl.catchgo.app.domain.model.JobOffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchEngineTest {

    /**
     * CP-31: Verificar que el motor de emparejamiento calcule la distancia correcta usando Haversine.
     */
    @Test
    fun calculateDistance_withValidCoordinates_returnsCorrectDistanceInKm() {
        // Coordenadas aproximadas en Santiago de Chile
        // Plaza de Armas: lat -33.4372, lon -70.6506
        // Costanera Center: lat -33.4190, lon -70.6064 (aprox. 4.6 km en línea recta)
        val lat1 = -33.4372
        val lon1 = -70.6506
        val lat2 = -33.4190
        val lon2 = -70.6064

        val distance = MatchEngine.calculateDistance(lat1, lon1, lat2, lon2)

        // Verificar que la distancia esté en el rango esperado (~4.6 km) con un delta tolerante de 0.2 km
        assertEquals(4.6, distance, 0.2)
    }

    /**
     * CP-32: Verificar que el cálculo de distancia geográfica retorne -1.0 si alguna de las coordenadas es nula.
     */
    @Test
    fun calculateDistance_withNullCoordinates_returnsNegativeOne() {
        val distance = MatchEngine.calculateDistance(null, -70.6506, -33.4190, null)
        assertEquals(-1.0, distance, 0.0)
    }

    /**
     * CP-33: Verificar el cálculo de completitud de perfil (Profile Completion) en 100% cuando todos los datos están presentes.
     */
    @Test
    fun calculateProfileCompletion_withAllFieldsPopulated_returnsOneHundredPercent() {
        val profile = ProfileRemoteDto(
            name = "Juan Pérez",
            email = "juan.perez@email.com",
            phone = "+56912345678",
            photoUrl = "http://imagenes.com/foto.jpg",
            cvUrl = "http://documentos.com/cv.pdf",
            latitude = -33.45,
            longitude = -70.65,
            bankName = "Banco de Chile",
            accountType = "Rut",
            accountNumber = "191234567",
            skills = "{\"habilidades\":[\"Electricidad\"],\"ambiente\":\"Dinámico\"}"
        )

        val completion = MatchEngine.calculateProfileCompletion(profile)
        assertEquals(100, completion)
    }

    /**
     * CP-34: Verificar el cálculo de completitud de perfil al 0% ante un objeto nulo o vacío.
     */
    @Test
    fun calculateProfileCompletion_withNullOrEmptyProfile_returnsZeroPercent() {
        val completion = MatchEngine.calculateProfileCompletion(null)
        assertEquals(0, completion)

        val emptyProfile = ProfileRemoteDto()
        val completionEmpty = MatchEngine.calculateProfileCompletion(emptyProfile)
        assertEquals(0, completionEmpty)
    }

    /**
     * CP-34 (Alterno): Verificar completitud de perfil parcial cuando faltan algunos documentos y datos bancarios.
     */
    @Test
    fun calculateProfileCompletion_withPartialFields_returnsProportionalPercent() {
        // Solo datos básicos ingresados (nombre, email, teléfono, ubicación)
        val profile = ProfileRemoteDto(
            name = "Juan Pérez",
            email = "juan.perez@email.com",
            phone = "+56912345678",
            latitude = -33.45,
            longitude = -70.65
        )

        val completion = MatchEngine.calculateProfileCompletion(profile)
        // La completitud debe ser mayor que 0 y estrictamente menor que 100
        assertTrue(completion in 1..99)
    }

    /**
     * CP-35: Verificar el puntaje de emparejamiento (Matching Score) para un plan TRIAL básico.
     */
    @Test
    fun calculateScore_forTrialPlan_calculatesBasicMatchingScore() {
        // Trabajador en Plaza de Armas con 5 estrellas de calificación y habilidades
        val worker = ProfileRemoteDto(
            latitude = -33.4372,
            longitude = -70.6506,
            rating = 5.0,
            description = "Tengo 5 años de experiencia trabajando en electricidad.",
            skills = "{\"habilidades\":[\"Electricidad\"],\"preferencia\":\"Electricista\",\"caracteristica\":\"Responsable\",\"ambiente\":\"Normal\"}"
        )

        // Empresa en Plaza de Armas (0 km de distancia)
        val company = ProfileRemoteDto(
            latitude = -33.4372,
            longitude = -70.6506,
            skills = "{\"habilidadValorada\":\"Responsabilidad\",\"ritmo\":\"Normal\"}"
        )

        // Oferta de trabajo para Electricista
        val offer = JobOffer(
            id = "101",
            title = "Oferta Electricista de Turno",
            company = "Electricidad Chile",
            category = JobCategory.OTRO,
            region = "Metropolitana",
            comuna = "Santiago",
            jornada = "Part-time",
            scheduleText = "Lunes a Viernes",
            salaryClp = 5000,
            salaryUnit = "hr",
            description = "Se requiere electricista con experiencia en cableado.",
            requirements = listOf("Electricidad"),
            score = 0,
            latitude = -33.4372,
            longitude = -70.6506
        )

        // Calcular coincidencia con plan TRIAL
        val score = MatchEngine.calculate(worker, company, offer, plan = "TRIAL")

        // El score debe estar bien calculado y cercano a 100 dado que coinciden habilidades, experiencia, distancia y disponibilidad
        assertTrue(score >= 80)
    }

    /**
     * CP-36: Verificar que un plan PREMIUM aplique un incremento del 20% al puntaje de emparejamiento (Matching Score).
     */
    @Test
    fun calculateScore_forPremiumPlan_appliesBonusMultiplier() {
        val worker = ProfileRemoteDto(
            latitude = -33.4372,
            longitude = -70.6506,
            rating = 4.5,
            description = "Electricista",
            skills = "{\"habilidades\":[\"Electricidad\"]}"
        )

        val company = ProfileRemoteDto(
            latitude = -33.4372,
            longitude = -70.6506
        )

        val offer = JobOffer(
            id = "102",
            title = "Ayudante general",
            company = "Reclutamiento temporal",
            category = JobCategory.OTRO,
            region = "Metropolitana",
            comuna = "Santiago",
            jornada = "Diurna",
            scheduleText = "Sábado",
            salaryClp = 4000,
            salaryUnit = "hr",
            description = "Apoyo en bodega.",
            requirements = emptyList(),
            score = 0,
            latitude = -33.4372,
            longitude = -70.6506
        )

        val scoreTrial = MatchEngine.calculate(worker, company, offer, plan = "TRIAL")
        val scorePremium = MatchEngine.calculate(worker, company, offer, plan = "PREMIUM")

        // Comprobar que el plan premium tiene mayor relevancia y aplica el multiplicador de bono del 20%
        if (scoreTrial in 1..83) { // Evitar desbordes por tope de 100 puntos
            assertTrue(scorePremium > scoreTrial)
            assertEquals((scoreTrial * 1.20).toInt(), scorePremium)
        }
    }
}
