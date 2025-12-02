import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ari.drup.data.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

val Context.dataStore by preferencesDataStore(name = "user_prefs")

object UserCache {
    private val USER_KEY = stringPreferencesKey("cached_user")
    private val EMAIL_KEY = stringPreferencesKey("cached_email")

    suspend fun saveUser(context: Context, user: User, email: String) {
        val json = Json.encodeToString(user)
        context.dataStore.edit { prefs ->
            prefs[USER_KEY] = json
            prefs[EMAIL_KEY] = email
        }
    }

    val cachedUserFlow: (Context) -> Flow<User?> = { context ->
        context.dataStore.data.map { prefs ->
            prefs[USER_KEY]?.let { Json.decodeFromString<User>(it) }
        }
    }

    val cachedEmailFlow: (Context) -> Flow<String?> = { context ->
        context.dataStore.data.map { prefs ->
            prefs[EMAIL_KEY]
        }
    }

    suspend fun clearUser(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(USER_KEY)
            prefs.remove(EMAIL_KEY)
        }
    }
}
