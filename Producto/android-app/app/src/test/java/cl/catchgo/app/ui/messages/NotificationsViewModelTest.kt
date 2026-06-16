package cl.catchgo.app.ui.messages

import androidx.lifecycle.viewModelScope
import cl.catchgo.app.domain.model.AppNotification
import cl.catchgo.app.domain.repository.NotificationRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {

    private val notificationRepository: NotificationRepository = mockk(relaxed = true)

    // Flow mutable para simular los estados del repositorio
    private val notificationsFlow = MutableStateFlow<List<AppNotification>>(emptyList())
    private val isConnectedFlow = MutableStateFlow(false)

    private lateinit var viewModel: NotificationsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val collectJobs = mutableListOf<Job>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Configurar retornos de flows del repositorio mockeado
        every { notificationRepository.notifications } returns notificationsFlow
        every { notificationRepository.isConnected } returns isConnectedFlow

        viewModel = NotificationsViewModel(notificationRepository)

        // Activar WhileSubscribed/Lazily recolectando los flujos del ViewModel
        val job1 = viewModel.viewModelScope.launch(testDispatcher) { viewModel.notifications.collect {} }
        val job2 = viewModel.viewModelScope.launch(testDispatcher) { viewModel.isConnected.collect {} }
        val job3 = viewModel.viewModelScope.launch(testDispatcher) { viewModel.unreadCount.collect {} }
        collectJobs.addAll(listOf(job1, job2, job3))
    }

    @After
    fun tearDown() {
        collectJobs.forEach { it.cancel() }
        collectJobs.clear()
        Dispatchers.resetMain()
    }

    /**
     * CP-52: Verificar el estado inicial por defecto de NotificationsViewModel (vacio y desconectado).
     */
    @Test
    fun init_defaultState_isCorrect() {
        assertTrue(viewModel.notifications.value.isEmpty())
        assertFalse(viewModel.isConnected.value)
        assertEquals(0, viewModel.unreadCount.value)
    }

    /**
     * CP-53: Verificar que el recuento de no leídas (unreadCount) se actualice correctamente cuando cambia la lista.
     */
    @Test
    fun unreadCount_onNotificationUpdate_isCorrect() {
        val list = listOf(
            AppNotification("1", "Nueva Oferta", "Conserje disponible", "info", 1000L, false),
            AppNotification("2", "Pago recibido", "Su transferencia fue confirmada", "success", 2000L, true),
            AppNotification("3", "Alerta de Match", "Match prioritario en su zona", "warning", 3000L, false)
        )
        notificationsFlow.value = list

        assertEquals(3, viewModel.notifications.value.size)
        assertEquals(2, viewModel.unreadCount.value) // 1 y 3 tienen isRead = false
    }

    /**
     * CP-54: Verificar que markAsRead invoque el repositorio para marcar la notificación como leída.
     */
    @Test
    fun markAsRead_callsRepository() {
        viewModel.markAsRead("alerta_99")
        verify(exactly = 1) { notificationRepository.markAsRead("alerta_99") }
    }

    /**
     * CP-55: Verificar que clearUnread llame al repositorio para cada notificación no leída.
     */
    @Test
    fun clearUnread_callsRepositoryForUnreadNotifications() {
        val list = listOf(
            AppNotification("n1", "Titulo 1", "Msj 1", "info", 100L, false),
            AppNotification("n2", "Titulo 2", "Msj 2", "success", 200L, true),
            AppNotification("n3", "Titulo 3", "Msj 3", "warning", 300L, false)
        )
        notificationsFlow.value = list

        viewModel.clearUnread()

        // Debe llamar a markAsRead en el repositorio solo para las no leídas (n1 y n3)
        verify(exactly = 1) { notificationRepository.markAsRead("n1") }
        verify(exactly = 1) { notificationRepository.markAsRead("n3") }
        verify(exactly = 0) { notificationRepository.markAsRead("n2") }
    }

    /**
     * CP-56: Verificar que clearAll invoque la limpieza total en el repositorio.
     */
    @Test
    fun clearAll_callsRepositoryClearAll() {
        viewModel.clearAll()
        verify(exactly = 1) { notificationRepository.clearAll() }
    }
}
