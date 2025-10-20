package com.example.practica3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        applySelectedTheme()
        super.onCreate(savedInstanceState)
    }

    protected fun applySelectedTheme() {
        val prefs = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        val themeId = when (prefs.getString("selected_theme", "default")) {
            "guinda" -> R.style.Theme_MyApp_Guinda
            "azul" -> R.style.Theme_MyApp_Azul
            "oscuro" -> R.style.Base_Theme_Practica3
            else -> R.style.Theme_Practica3
        }
        setTheme(themeId)
    }

    protected fun recreateWithTheme() {
        recreate()
    }
}