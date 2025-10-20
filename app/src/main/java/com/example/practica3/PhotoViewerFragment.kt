package com.example.practica3

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.github.chrisbanes.photoview.PhotoView
import java.io.InputStream


class PhotoViewerFragment : DialogFragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnInfo: ImageButton
    private lateinit var btnDelete: ImageButton
    private lateinit var btnShare: ImageButton

    private var uris: List<Uri> = emptyList()
    private var startPosition: Int = 0

    companion object {
        fun newInstance(list: ArrayList<String>, startPosition: Int): PhotoViewerFragment {
            val f = PhotoViewerFragment()
            val b = Bundle()
            b.putStringArrayList("uris", list)
            b.putInt("start", startPosition)
            f.arguments = b
            return f
        }
    }

    // resultado uCrop
    private val uCropLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
            val resultUri = com.yalantis.ucrop.UCrop.getOutput(result.data!!)
            resultUri?.let {
                // actualizar MediaStore si es necesario
                parentFragmentManager.setFragmentResult("gallery_update", Bundle().apply { putString("action", "edited") })
                Toast.makeText(requireContext(), "Edición aplicada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val list = arguments?.getStringArrayList("uris") ?: arrayListOf()
        uris = list.map { Uri.parse(it) }
        startPosition = arguments?.getInt("start") ?: 0

        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dlg = super.onCreateDialog(savedInstanceState)
        dlg.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dlg
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_photo_viewer, container, false)
        btnInfo = root.findViewById(R.id.btn_info)
        btnDelete = root.findViewById(R.id.btn_delete)
        btnShare = root.findViewById(R.id.btn_share)

        val adapter = PhotoPagerAdapter(uris)
        viewPager.adapter = adapter
        viewPager.setCurrentItem(startPosition, false)

        btnInfo.setOnClickListener {
            showExif(uris[viewPager.currentItem])
        }

        btnDelete.setOnClickListener {
            confirmAndDelete(uris[viewPager.currentItem])
        }

        btnShare.setOnClickListener {
            shareImage(uris[viewPager.currentItem])
        }

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
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Información EXIF")
                .setMessage(exifText)
                .setPositiveButton("Aceptar", null)
                .show()
        } catch (e: Exception) {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Información EXIF")
                .setMessage("No se pudo leer EXIF")
                .setPositiveButton("Aceptar", null)
                .show()
        }
    }

    private fun confirmAndDelete(uri: Uri) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar foto")
            .setMessage("¿Desea eliminar esta foto?")
            .setPositiveButton("Sí") { _, _ ->
                try {
                    val rows = requireContext().contentResolver.delete(uri, null, null)
                    if (rows > 0) {
                        parentFragmentManager.setFragmentResult("gallery_update", Bundle().apply { putString("action", "deleted") })
                        // Si borramos, cerramos el viewer o avanzamos
                        if (uris.size <= 1) dismiss() else {
                            // actualizar lista local y adaptador
                            val newList = uris.toMutableList()
                            newList.removeAt(viewPager.currentItem)
                            (viewPager.adapter as? PhotoPagerAdapter)?.updateList(newList)
                            // notify gallery to reload
                            uris = newList
                        }
                        Toast.makeText(requireContext(), "Foto eliminada", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "No se pudo eliminar", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: SecurityException) {
                    // En Android 10+ puede requerir usar MediaStore delete vía id o pedir permiso
                    Toast.makeText(requireContext(), "Error eliminando: permiso denegado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun shareImage(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Abrir chooser genérico para cualquier app que acepte imágenes
        startActivity(Intent.createChooser(intent, "Compartir imagen"))
    }

    private fun startCrop(uri: Uri) {
        // Comprueba si UCrop está disponible (si no, muestra mensaje)
        try {
            val destUri = Uri.fromFile(requireContext().cacheDir.resolve("ucrop_${System.currentTimeMillis()}.jpg"))
            val uCrop = com.yalantis.ucrop.UCrop.of(uri, destUri)
            uCrop.withAspectRatio(0f, 0f) // libre
            uCrop.withMaxResultSize(4096, 4096)
            uCropLauncher.launch(uCrop.getIntent(requireContext()))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Edición no disponible", Toast.LENGTH_SHORT).show()
        }
    }

    // Adapter interno para ViewPager2
    private inner class PhotoPagerAdapter(var list: List<Uri>) : RecyclerView.Adapter<PhotoPagerAdapter.Holder>() {

        inner class Holder(item: View) : RecyclerView.ViewHolder(item) {
            val photoView: PhotoView = item.findViewById(R.id.pageImage)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val v = layoutInflater.inflate(R.layout.item_photo_page, parent, false)
            return Holder(v)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val uri = list[position]
            // Cargar la imagen (PhotoView soporta setImageURI)
            holder.photoView.setImageURI(uri)
        }

        override fun getItemCount(): Int = list.size

        fun updateList(newList: List<Uri>) {
            this.list = newList
            notifyDataSetChanged()
            if (newList.isNotEmpty()) {
                val pos = kotlin.math.min(viewPager.currentItem, newList.size - 1)
                viewPager.setCurrentItem(pos, false)
            } else {
                dismiss()
            }
        }
    }
}
