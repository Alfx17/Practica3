package com.example.practica3

import android.os.PersistableBundle
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.practica3.databinding.Activity2Binding
import androidx.fragment.app.commit
import android.widget.FrameLayout
import android.widget.TextView
import android.view.Gravity
import android.view.ViewGroup
import android.view.View
import androidx.constraintlayout.widget.Placeholder

class Activity2 : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_2)

        val module = intent?.getStringExtra("module") ?: "camera"

        when (module){
            "camera" -> {
                supportFragmentManager.commit{
                    replace(R.id.contenedorFragment, CamaraFragment())
                }
            }
            "gallery" -> {
                supportFragmentManager.commit{
                    replace(R.id.contenedorFragment, GaleriaFragment())
                }
            }
            "audio" -> {
                supportFragmentManager.commit{
                    replace(R.id.contenedorFragment, AudioFragment())
                }
            }
            else -> {
                supportFragmentManager.commit{
                    replace(R.id.contenedorFragment, CamaraFragment())
                }
            }
        }
    }
}