package com.satanas1275.notes.data

enum class BackgroundStyle {
    GRADIENT,
    SOLID_COLOR,
    IMAGE
}

data class AppSettings(
    val backgroundStyle: BackgroundStyle = BackgroundStyle.GRADIENT,
    val backgroundColorIndex: Int = 0,
    val hasCustomBackgroundImage: Boolean = false,
    val cloudSyncEnabled: Boolean = false,
    // null = suit la langue du système. Sinon un tag BCP-47 ("fr", "en", ...).
    val languageTag: String? = null
)
