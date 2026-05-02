package cl.catchgo.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "catchgo_auth")

@Singleton
class AuthTokenStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val token: Flow<String?> = context.dataStore.data.map { it[KEY_TOKEN] }

    suspend fun save(token: String) {
        context.dataStore.edit { it[KEY_TOKEN] = token }
    }

    suspend fun clear() {
        context.dataStore.edit { it.remove(KEY_TOKEN) }
    }

    private companion object {
        val KEY_TOKEN = stringPreferencesKey("jwt_token")
    }
}
