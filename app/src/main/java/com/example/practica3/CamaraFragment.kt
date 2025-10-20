package com.example.practica3

import android.content.ContentValues
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CamaraFragment : Fragment() {

    private lateinit var previewView: PreviewView
    private lateinit var btnCapture: Button
    private lateinit var btnSwitchCamera: Button
    private lateinit var btnFlash: Button
    private lateinit var btnTimer: Button

    private lateinit var btnR: Button

    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var flashMode = ImageCapture.FLASH_MODE_OFF
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private lateinit var cameraExecutor: ExecutorService
    private var timerSeconds = 0L
    private var isTakingPhoto = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_camara, container, false)
        previewView = view.findViewById(R.id.previewView)
        btnCapture = view.findViewById(R.id.btn_capture)
        btnSwitchCamera = view.findViewById(R.id.btn_switch_camera)
        btnFlash = view.findViewById(R.id.btn_flash)
        btnTimer = view.findViewById(R.id.btn_timer)
        btnR = view.findViewById(R.id.buttonR)

        cameraExecutor = Executors.newSingleThreadExecutor()

        btnSwitchCamera.setOnClickListener {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                CameraSelector.DEFAULT_FRONT_CAMERA
            else
                CameraSelector.DEFAULT_BACK_CAMERA
            startCamera()
        }

        btnFlash.setOnClickListener {
            flashMode = when (flashMode) {
                ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                else -> ImageCapture.FLASH_MODE_OFF
            }
            updateFlashMode()
            Toast.makeText(requireContext(), "Flash: ${getFlashModeText()}", Toast.LENGTH_SHORT).show()
        }

        btnTimer.setOnClickListener {
            val options = arrayOf(0L, 3L, 5L, 10L)
            val currentIndex = options.indexOf(timerSeconds)
            val nextIndex = (currentIndex + 1) % options.size
            timerSeconds = options[nextIndex]

            Toast.makeText(requireContext(),
                if (timerSeconds == 0L) "Temporizador: OFF" else "Temporizador: $timerSeconds s",
                Toast.LENGTH_SHORT).show()
        }

        btnCapture.setOnClickListener {
            if (!isTakingPhoto) {
                takePhotoWithTimer()
            }
        }

        btnR.setOnClickListener {
            requireActivity().finish()
        }

        startCamera()
        return view
    }

    private fun getFlashModeText(): String {
        return when (flashMode) {
            ImageCapture.FLASH_MODE_OFF -> "OFF"
            ImageCapture.FLASH_MODE_ON -> "ON"
            ImageCapture.FLASH_MODE_AUTO -> "AUTO"
            else -> "OFF"
        }
    }

    private fun updateFlashMode() {
        // Actualizar el flash mode en imageCapture si existe
        imageCapture?.flashMode = flashMode

        // Si hay una cámara activa, forzar la reconexión para aplicar el flash
        if (camera != null) {
            previewView.postDelayed({
                startCamera()
            }, 100)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            // Configurar ImageCapture con el modo de flash actual
            imageCapture = ImageCapture.Builder()
                .setFlashMode(flashMode)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                // Configurar el flash en la cámara después de enlazar
                camera?.cameraControl?.enableTorch(flashMode == ImageCapture.FLASH_MODE_ON)

            } catch (exc: Exception) {
                Log.e("CamaraFragment", "Error binding camera use cases", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhotoWithTimer() {
        if (isTakingPhoto) return

        isTakingPhoto = true

        // Asegurar que el flash esté configurado antes de cualquier delay
        imageCapture?.flashMode = flashMode

        if (timerSeconds > 0) {
            object : android.os.CountDownTimer(timerSeconds * 1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    Toast.makeText(
                        requireContext(),
                        "Foto en ${millisUntilFinished / 1000} s",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onFinish() {
                    // Pre-activar el flash antes de tomar la foto
                    prepareFlashAndTakePhoto()
                }
            }.start()
        } else {
            prepareFlashAndTakePhoto()
        }
    }

    private fun prepareFlashAndTakePhoto() {
        // Si el flash está activado, dar tiempo extra para que se prepare
        val flashDelay = if (flashMode == ImageCapture.FLASH_MODE_ON) {
            // Más tiempo para el flash ON
            1000L
        } else if (flashMode == ImageCapture.FLASH_MODE_AUTO) {
            // Menos tiempo para AUTO
            500L
        } else {
            // Sin delay para flash OFF
            100L
        }

        previewView.postDelayed({
            takePhoto()
        }, flashDelay)
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: run {
            isTakingPhoto = false
            return
        }

        // Forzar la configuración del flash una última vez
        imageCapture.flashMode = flashMode

        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Practica3")
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            requireContext().contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        // Pequeño delay final para asegurar que todo esté listo
        previewView.postDelayed({
            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(requireContext()),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        Toast.makeText(requireContext(), "Foto guardada en galería", Toast.LENGTH_SHORT).show()
                        isTakingPhoto = false
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(
                            requireContext(),
                            "Error tomando foto: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("CamaraFragment", "Error taking photo", exception)
                        isTakingPhoto = false
                    }
                }
            )
        }, 200)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}