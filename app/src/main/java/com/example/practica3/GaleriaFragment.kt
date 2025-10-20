package com.example.practica3

import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GaleriaFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val photoList = mutableListOf<Uri>()
    private lateinit var adapter: GalleryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Escuchar resultado de eliminación / renombrado desde PhotoViewerFragment
        parentFragmentManager.setFragmentResultListener("gallery_update", this) { _, bundle ->
            val action = bundle.getString("action")
            if (action == "deleted" || action == "renamed") {
                loadPhotos()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_galeria, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewGallery)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        adapter = GalleryAdapter(photoList) { uri ->
            openPhotoViewer(uri)
        }
        recyclerView.adapter = adapter

        loadPhotos()
        return view
    }

    private fun loadPhotos() {
        photoList.clear()
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val cursor = requireContext().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, null, null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )

        cursor?.use {
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val imageUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                photoList.add(imageUri)
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun openPhotoViewer(uri: Uri) {
        // Llama a tu PhotoViewerFragment pasando la URI de la foto
        val frag = PhotoViewerFragment.newInstance(uri)
        frag.show(parentFragmentManager, "photo_viewer")

    }

    // Adapter interno
    class GalleryAdapter(
        private val items: List<Uri>,
        private val click: (Uri) -> Unit
    ) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val img: ImageView = view.findViewById(R.id.imgThumbnail)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_gallery_photo, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val uri = items[position]
            val context = holder.img.context

            try {
                val thumbnail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    context.contentResolver.loadThumbnail(uri, Size(200, 200), null)
                } else {
                    val id: Long = ContentUris.parseId(uri)
                    MediaStore.Images.Thumbnails.getThumbnail(
                        context.contentResolver,
                        id,
                        MediaStore.Images.Thumbnails.MINI_KIND,
                        null
                    )
                }
                holder.img.setImageBitmap(thumbnail)
            } catch (e: Exception) {
                e.printStackTrace()
                // Si falla, dejamos el ImageView vacío
                holder.img.setImageDrawable(null)
            }

            holder.img.setOnClickListener { click(uri) }
        }
    }
}
