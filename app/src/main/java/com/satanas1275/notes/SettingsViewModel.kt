package com.satanas1275.notes

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.satanas1275.notes.data.AppSettings
import com.satanas1275.notes.data.BackgroundStyle
import com.satanas1275.notes.data.SettingsRepository
import com.satanas1275.notes.data.SettingsRepository.backgroundImageFile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    val uiState: StateFlow<AppSettings> = SettingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setBackgroundStyle(style: BackgroundStyle) = SettingsRepository.setBackgroundStyle(style)

    fun setBackgroundColorIndex(index: Int) = SettingsRepository.setBackgroundColorIndex(index)

    fun pickBackgroundImage(uri: Uri) {
        viewModelScope.launch {
            SettingsRepository.setBackgroundImage(getApplication(), uri)
        }
    }

    fun clearBackgroundImage() = SettingsRepository.clearBackgroundImage(getApplication())

    fun setCloudSyncEnabled(enabled: Boolean) = SettingsRepository.setCloudSyncEnabled(enabled)

    fun setLanguageTag(tag: String?) = SettingsRepository.setLanguageTag(tag)

    fun backgroundImageFile() = getApplication<Application>().backgroundImageFile
}
