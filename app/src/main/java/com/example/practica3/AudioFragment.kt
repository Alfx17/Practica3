package com.example.practica3

import android.content.Context
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AudioFragment : Fragment() {

    private lateinit var btnRecord: Button
    private lateinit var btnPauseResume: Button
    private lateinit var btnStop: Button
    private lateinit var btnR: Button
    private lateinit var recyclerView: RecyclerView

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var recordingFile: File? = null

    private val recordings = mutableListOf<File>()
    private lateinit var adapter: RecordingAdapter

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_audio, container, false)

        btnRecord = view.findViewById(R.id.btnRecord)
        btnPauseResume = view.findViewById(R.id.btnPauseResume)
        btnStop = view.findViewById(R.id.btnStop)
        btnR = view.findViewById(R.id.btnR)
        recyclerView = view.findViewById(R.id.recyclerViewRecordings)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = RecordingAdapter(requireContext(), recordings,
            renameCallback = { oldFile, newFile ->
                val index = recordings.indexOf(oldFile)
                if (index != -1) {
                    recordings[index] = newFile
                    adapter.notifyDataSetChanged()
                }
            },
            deleteCallback = { file ->
                recordings.remove(file)
                adapter.notifyDataSetChanged()
            }
        ) { file -> playRecording(file) }

        recyclerView.adapter = adapter

        btnRecord.setOnClickListener { startRecording() }
        btnPauseResume.setOnClickListener { pauseResumeRecording() }
        btnStop.setOnClickListener { stopRecording() }
        btnR.setOnClickListener { requireActivity().finish() }

        loadRecordings()

        return view
    }

    private fun startRecording() {
        val dir = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC), "Grabaciones")
        if (!dir.exists()) dir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        recordingFile = File(dir, "P3Audio_$timestamp.m4a")

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(recordingFile!!.absolutePath)
            prepare()
            start()
        }

        btnRecord.isEnabled = false
        btnPauseResume.isEnabled = true
        btnStop.isEnabled = true
    }

    private fun pauseResumeRecording() {
        mediaRecorder?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (btnPauseResume.text == "Pausar") {
                    it.pause()
                    btnPauseResume.text = "Reanudar"
                } else {
                    it.resume()
                    btnPauseResume.text = "Pausar"
                }
            } else {
                Toast.makeText(requireContext(), "Pausar/Reanudar no disponible en esta versión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null

        recordingFile?.let { recordings.add(it) }
        adapter.notifyDataSetChanged()
        recordingFile = null

        btnRecord.isEnabled = true
        btnPauseResume.isEnabled = false
        btnPauseResume.text = "Pausar"
        btnStop.isEnabled = false
    }

    private fun playRecording(file: File) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            prepare()
            start()
        }
        adapter.setPlayingFile(file, mediaPlayer!!)
    }

    private fun loadRecordings() {
        val dir = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC), "Grabaciones")
        if (dir.exists()) {
            recordings.clear()
            recordings.addAll(dir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList())
            adapter.notifyDataSetChanged()
        }
    }

    class RecordingAdapter(
        private val context: Context,
        private val items: List<File>,
        private val renameCallback: (oldFile: File, newFile: File) -> Unit,
        private val deleteCallback: (file: File) -> Unit,
        private val click: (File) -> Unit
    ) : RecyclerView.Adapter<RecordingAdapter.ViewHolder>() {

        private var playingFile: File? = null
        private var mediaPlayer: MediaPlayer? = null
        private val handler = Handler(Looper.getMainLooper())

        fun setPlayingFile(file: File, player: MediaPlayer) {
            playingFile = file
            mediaPlayer = player
            notifyDataSetChanged()
            updateProgress()
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val txtName: TextView = view.findViewById(R.id.txtRecordingName)
            val txtInfo: TextView = view.findViewById(R.id.txtRecordingInfo)
            val seekBar: SeekBar = view.findViewById(R.id.seekBarProgress)
            val btnShare: ImageButton = view.findViewById(R.id.btnShareRecording)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recording, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val file = items[position]
            holder.txtName.text = file.name

            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt() ?: 0
            retriever.release()
            holder.txtInfo.text = "${durationMs/1000}s - ${sdf.format(Date(file.lastModified()))}"

            holder.seekBar.max = durationMs
            holder.seekBar.progress = 0

            holder.itemView.setOnClickListener { click(file) }
            holder.itemView.setOnLongClickListener {
                showItemOptions(file)
                true
            }

            holder.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser && mediaPlayer != null && file == playingFile) {
                        mediaPlayer?.seekTo(progress)
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            holder.btnShare.setOnClickListener {
                val fileUri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND)
                intent.type = "audio/*"
                intent.putExtra(android.content.Intent.EXTRA_STREAM, fileUri)
                intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(android.content.Intent.createChooser(intent, "Compartir grabación"))
            }
        }

        private fun updateProgress() {
            mediaPlayer?.let { player ->
                playingFile?.let { file ->
                    val index = items.indexOf(file)
                    if (index != -1) {
                        val holder = recyclerView?.findViewHolderForAdapterPosition(index) as? ViewHolder
                        holder?.seekBar?.progress = player.currentPosition
                        if (player.isPlaying) handler.postDelayed({ updateProgress() }, 500)
                    }
                }
            }
        }

        private val recyclerView: RecyclerView?
            get() = (context as? androidx.fragment.app.FragmentActivity)?.findViewById(R.id.recyclerViewRecordings)

        private fun showItemOptions(file: File) {
            val options = arrayOf("Renombrar", "Eliminar", "Compartir")

            val dialog = androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle(file.name)
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> renameFile(file)
                        1 -> deleteFile(file)
                        2 -> shareFile(file)
                    }
                }
                .create()

            dialog.setOnShowListener {
                // Usar el mismo ID que funcionó en renameFile
                val titleView = dialog.findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
                titleView?.setTextColor(Color.BLACK)
            }

            dialog.show()
        }

        private fun renameFile(file: File) {
            val input = EditText(context).apply {
                setText(file.nameWithoutExtension)
                // Asegurar que el texto del input sea visible
                setTextColor(Color.BLACK)  // Texto negro
                setHintTextColor(Color.GRAY) // Hint en gris si lo usas
            }

            val dialog = androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Renombrar grabación")
                .setView(input)
                .setPositiveButton("Aceptar") { _, _ ->
                    val newName = input.text.toString().trim()
                    if (newName.isNotEmpty()) {
                        val newFile = File(file.parentFile, "$newName.${file.extension}")
                        if (file.renameTo(newFile)) renameCallback(file, newFile)
                    }
                }
                .setNegativeButton("Cancelar", null)
                .create()

            dialog.setOnShowListener {
                val titleView = dialog.findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
                titleView?.setTextColor(Color.BLACK)

                // Los botones también en negro para consistencia
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLACK)
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.BLACK)

                // El input ya tiene su color configurado arriba, no lo cambiamos aquí
            }

            dialog.show()
        }

        private fun deleteFile(file: File) {
            androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Eliminar grabación")
                .setMessage("¿Desea eliminar ${file.name}?")
                .setPositiveButton("Sí") { _, _ ->
                    if (file.delete()) deleteCallback(file)
                }
                .setNegativeButton("No", null)
                .show()
        }

        private fun shareFile(file: File) {
            val fileUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND)
            intent.type = "audio/*"
            intent.putExtra(android.content.Intent.EXTRA_STREAM, fileUri)
            intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(android.content.Intent.createChooser(intent, "Compartir grabación"))
        }

        override fun getItemCount(): Int = items.size
    }
}
