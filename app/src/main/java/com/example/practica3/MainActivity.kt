package com.example.practica3

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
class MainActivity : BaseActivity() {

    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )
    private var pendingModuleToOpen: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnGuinda = findViewById<Button>(R.id.btnGuinda)
        val btnAzul = findViewById<Button>(R.id.btnAzul)
        val btnOscuro = findViewById<Button>(R.id.btnOscuro)

        btnGuinda.setOnClickListener {
            getSharedPreferences("theme_prefs", MODE_PRIVATE).edit {
                putString("selected_theme", "guinda")
            }
            recreateWithTheme()
        }

        btnAzul.setOnClickListener {
            getSharedPreferences("theme_prefs", MODE_PRIVATE).edit {
                putString("selected_theme", "azul")
            }
            recreateWithTheme()
        }

        btnOscuro.setOnClickListener {
            getSharedPreferences("theme_prefs", MODE_PRIVATE).edit {
                putString("selected_theme", "oscuro")
            }
            recreateWithTheme()
        }

        val btnCamera = findViewById<Button>(R.id.btn_camera)
        val btnGallery = findViewById<Button>(R.id.btn_gallery)
        val btnAudio = findViewById<Button>(R.id.btn_audio)

        btnCamera.setOnClickListener { ensurePermissionsThenOpenModule("camera") }
        btnGallery.setOnClickListener { ensurePermissionsThenOpenModule("gallery") }
        btnAudio.setOnClickListener { ensurePermissionsThenOpenModule("audio") }
    }

    private fun ensurePermissionsThenOpenModule(module: String) {
        val missing = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) {
            openModule(module)
        } else {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 1001)
            pendingModuleToOpen = module
        }
    }

    private fun openModule(module: String){
        val intent = Intent(this, Activity2::class.java)
        intent.putExtra("module", module)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            val denied = grantResults.indices.any { grantResults[it] != PackageManager.PERMISSION_GRANTED }
            if (denied) {
                Toast.makeText(this, "Permisos necesarios denegados.", Toast.LENGTH_LONG).show()
            } else {
                pendingModuleToOpen?.let { openModule(it) }
            }
            pendingModuleToOpen = null
        }
    }
}