package cl.catchgo.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cl.catchgo.app.domain.model.User
import cl.catchgo.app.domain.model.UserRole
import cl.catchgo.app.domain.model.UserSession
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "catchgo_session")

@Singleton
class SessionStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val session: Flow<UserSession?> = context.dataStore.data.map { prefs ->
        val token = prefs[KEY_TOKEN] ?: return@map null
        UserSession(
            token = token,
            user = User(
                id = prefs[KEY_USER_ID] ?: "",
                email = prefs[KEY_USER_EMAIL] ?: "",
                role = prefs[KEY_USER_ROLE]
                    ?.let { runCatching { UserRole.valueOf(it) }.getOrDefault(UserRole.UNKNOWN) }
                    ?: UserRole.UNKNOWN,
                fullName = prefs[KEY_USER_FULL_NAME],
                nivel = prefs[KEY_USER_NIVEL] ?: 1
            )
        )
    }

    suspend fun save(session: UserSession) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = session.token
            prefs[KEY_USER_ID] = session.user.id
            prefs[KEY_USER_EMAIL] = session.user.email
            prefs[KEY_USER_ROLE] = session.user.role.name
            session.user.fullName?.let { prefs[KEY_USER_FULL_NAME] = it }
            prefs[KEY_USER_NIVEL] = session.user.nivel
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun token(): String? = session.first()?.token

    private companion object {
        val KEY_TOKEN = stringPreferencesKey("jwt_token")
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        val KEY_USER_ROLE = stringPreferencesKey("user_role")
        val KEY_USER_FULL_NAME = stringPreferencesKey("user_full_name")
        val KEY_USER_NIVEL = intPreferencesKey("user_nivel")
    }
}
