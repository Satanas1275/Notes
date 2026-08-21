package com.satanas1275.notes.data

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

private val BACKGROUND_STYLE_KEY = stringPreferencesKey("background_style")
private val BACKGROUND_COLOR_KEY = intPreferencesKey("background_color_index")
private val BACKGROUND_IMAGE_KEY = booleanPreferencesKey("has_custom_background_image")
private val CLOUD_SYNC_KEY = booleanPreferencesKey("cloud_sync_enabled")
private val LANGUAGE_KEY = stringPreferencesKey("language_tag")

object SettingsRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    @Volatile
    private var initialized = false

    private lateinit var appContext: Context

    /** Fichier où l'image de fond choisie par l'utilisateur est copiée. */
    val Context.backgroundImageFile: File
        get() = File(filesDir, "background_image.jpg")

    fun init(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            initialized = true
            appContext = context.applicationContext
            scope.launch {
                appContext.settingsDataStore.data.collect { preferences ->
                    _settings.value = AppSettings(
                        backgroundStyle = preferences[BACKGROUND_STYLE_KEY]
                            ?.let { runCatching { BackgroundStyle.valueOf(it) }.getOrNull() }
                            ?: BackgroundStyle.GRADIENT,
                        backgroundColorIndex = preferences[BACKGROUND_COLOR_KEY] ?: 0,
                        hasCustomBackgroundImage = preferences[BACKGROUND_IMAGE_KEY] ?: false,
                        cloudSyncEnabled = preferences[CLOUD_SYNC_KEY] ?: false,
                        languageTag = preferences[LANGUAGE_KEY]
                    )
                }
            }
        }
    }

    fun setBackgroundStyle(style: BackgroundStyle) {
        edit { it[BACKGROUND_STYLE_KEY] = style.name }
    }

    fun setBackgroundColorIndex(index: Int) {
        edit { it[BACKGROUND_COLOR_KEY] = index }
    }

    /**
     * Copie l'image choisie (URI du sélecteur système) dans le stockage privé
     * de l'app, pour ne pas dépendre d'une permission d'URI persistable.
     */
    fun setBackgroundImage(context: Context, uri: Uri): Boolean {
        val ok = runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                context.applicationContext.backgroundImageFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } != null
        }.getOrDefault(false)
        if (ok) {
            edit { it[BACKGROUND_IMAGE_KEY] = true }
        }
        return ok
    }

    fun clearBackgroundImage(context: Context) {
        runCatching { context.applicationContext.backgroundImageFile.delete() }
        edit { it[BACKGROUND_IMAGE_KEY] = false }
    }

    fun setCloudSyncEnabled(enabled: Boolean) {
        edit { it[CLOUD_SYNC_KEY] = enabled }
    }

    fun setLanguageTag(tag: String?) {
        edit {
            if (tag == null) it.remove(LANGUAGE_KEY) else it[LANGUAGE_KEY] = tag
        }
    }

    private fun edit(transform: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        if (!initialized) return
        scope.launch {
            appContext.settingsDataStore.edit { preferences -> transform(preferences) }
        }
    }
}
