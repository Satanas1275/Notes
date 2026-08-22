package com.satanas1275.notes

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.satanas1275.notes.data.NotesRepository
import com.satanas1275.notes.data.SettingsRepository
import com.satanas1275.notes.ui.theme.NotesTheme

// AppCompatActivity (et non ComponentActivity) est nécessaire pour que
// AppCompatDelegate.setApplicationLocales() applique réellement la langue
// choisie sur Android < 13 (API 33) : en dessous de cette version, c'est
// AppCompatActivity#attachBaseContext qui enveloppe le Contexte avec la
// bonne locale. Avec un simple ComponentActivity, le choix était bien
// persisté mais jamais appliqué visuellement, d'où l'app restée en français.
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotesRepository.init(this)
        SettingsRepository.init(this)
        enableEdgeToEdge()
        setContent {
            NotesTheme {
                NotesApp()
            }
        }
    }
}
