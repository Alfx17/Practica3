package com.example.practica3

import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import java.io.InputStream
import android.media.ExifInterface

class PhotoViewerFragment : DialogFragment() {

    private lateinit var imgPhoto: ImageView
    private lateinit var btnInfo: ImageButton
    private lateinit var btnDelete: ImageButton
    private lateinit var btnShare: ImageButton

    private lateinit var photoUri: Uri

    companion object {
        fun newInstance(uri: Uri): PhotoViewerFragment {
            val fragment = PhotoViewerFragment()
            val bundle = Bundle()
            bundle.putParcelable("photo_uri", uri)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        photoUri = arguments?.getParcelable("photo_uri") ?: Uri.EMPTY
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) = super.onCreateDialog(savedInstanceState).apply {
        window?.requestFeature(Window.FEATURE_NO_TITLE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_photo_viewer, container, false)

        imgPhoto = root.findViewById(R.id.imgPhotoViewer)
        btnInfo = root.findViewById(R.id.btn_info)
        btnDelete = root.findViewById(R.id.btn_delete)
        btnShare = root.findViewById(R.id.btn_share)

        // Mostrar la foto
        imgPhoto.setImageURI(photoUri)

        btnInfo.setOnClickListener { showExif(photoUri) }
        btnDelete.setOnClickListener { confirmAndDelete(photoUri) }
        btnShare.setOnClickListener { shareImage(photoUri) }

        return root
    }

    private fun showExif(uri: Uri) {
        try {
            val input: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val exif = input?.let { ExifInterface(it) }
            val exifText = if (exif != null) {
                """
                Fecha: ${exif.getAttribute(ExifInterface.TAG_DATETIME)}
                Resolución: ${exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH)} x ${exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH)}
                Flash: ${exif.getAttribute(ExifInterface.TAG_FLASH)}
                Orientación: ${exif.getAttribute(ExifInterface.TAG_ORIENTATION)}
                """.trimIndent()
            } else {
                "No se pudo leer EXIF"
            }
            input?.close()

            val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Información EXIF")
                .setMessage(exifText)
                .setPositiveButton("Aceptar", null)
                .create()

            dialog.setOnShowListener {
                val titleView = dialog.findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
                val messageView = dialog.findViewById<TextView>(android.R.id.message)

                titleView?.setTextColor(Color.BLACK)
                messageView?.setTextColor(Color.BLACK)

                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLACK)
            }

            dialog.show()
        } catch (e: Exception) {
            val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Información EXIF")
                .setMessage("No se pudo leer EXIF")
                .setPositiveButton("Aceptar", null)
                .create()

            dialog.setOnShowListener {
                val titleView = dialog.findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
                val messageView = dialog.findViewById<TextView>(android.R.id.message)

                titleView?.setTextColor(Color.BLACK)
                messageView?.setTextColor(Color.BLACK)

                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLACK)
            }

            dialog.show()
        }
    }

    private fun confirmAndDelete(uri: Uri) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar foto")
            .setMessage("¿Desea eliminar esta foto?")
            .setPositiveButton("Sí") { _, _ ->
                try {
                    val rows = requireContext().contentResolver.delete(uri, null, null)
                    if (rows > 0) {
                        Toast.makeText(requireContext(), "Foto eliminada", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.setFragmentResult("gallery_update", Bundle().apply { putString("action", "deleted") })
                        dismiss()
                    } else {
                        Toast.makeText(requireContext(), "No se pudo eliminar", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: SecurityException) {
                    Toast.makeText(requireContext(), "Error eliminando: permiso denegado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No", null)
            .create()

        dialog.setOnShowListener {
            val titleView = dialog.findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
            val messageView = dialog.findViewById<TextView>(android.R.id.message)

            titleView?.setTextColor(Color.BLACK)
            messageView?.setTextColor(Color.BLACK)

            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLACK)
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.BLACK)
        }

        dialog.show()
    }

    private fun shareImage(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Compartir imagen"))
    }
}