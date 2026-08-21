package com.satanas.notes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.satanas.notes.data.NotesRepository
import com.satanas.notes.ui.theme.NotesTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotesRepository.init(this)
        enableEdgeToEdge()
        setContent {
            NotesTheme {
                NotesApp()
            }
        }
    }
}
