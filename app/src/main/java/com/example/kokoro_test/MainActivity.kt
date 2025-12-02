package com.example.kokoro_test

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    private var kokoroEngine: KokoroEngine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputText = findViewById<EditText>(R.id.inputText)
        val btnSpeak = findViewById<Button>(R.id.btnSpeak)

        // Initialize Kokoro TTS engine
        try {
            kokoroEngine = KokoroEngine(assets)
            Toast.makeText(this, "TTS engine ready", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to init TTS: ${e.message}", Toast.LENGTH_LONG).show()
        }

        btnSpeak.setOnClickListener {
            val text = inputText.text.toString()

            if (text.isBlank()) {
                Toast.makeText(this, "Please type some text", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val engine = kokoroEngine
            if (engine == null) {
                Toast.makeText(this, "TTS engine not ready", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Speak the text
            engine.speak(text)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        kokoroEngine?.close()
        kokoroEngine = null
    }
}