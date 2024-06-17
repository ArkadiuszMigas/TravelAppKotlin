package com.example.kotlinapp

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class TravelDetails : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var travelId: String
    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var detailsData: DocumentReference
    var travelDoc = mutableMapOf<String,Any>()
    private val notesList = mutableListOf<Note>()
    private lateinit var currentPhotoPath: String
    private lateinit var photoUri: Uri

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            dispatchTakePictureIntent()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.travel_details)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        travelId = intent.getStringExtra("TRAVEL_ID") ?: ""
        detailsData = firestore.collection("Users").document(auth.currentUser!!.uid)
            .collection("travels").document(travelId)

        setupUI()
        loadNotes()
    }

    private fun setupUI() {
        val travelTitleTextView: TextView = findViewById(R.id.travelName)
        val addNoteButton: Button = findViewById(R.id.addNoteButton)

        travelTitleTextView.text = travelId

        notesRecyclerView = findViewById(R.id.notesRecyclerView)
        notesRecyclerView.layoutManager = LinearLayoutManager(this)
        notesAdapter = NotesAdapter(notesList)
        notesRecyclerView.adapter = notesAdapter

        addNoteButton.setOnClickListener {
            showAddNoteDialog()
        }
    }

    private fun loadNotes() {
        detailsData.get().addOnSuccessListener { document ->
            if(document!=null) {
                travelDoc = document.data!!
            }
        }
        firestore.collection("Users").document(auth.currentUser!!.uid)
            .collection("travels").document(travelId)
            .collection("details").document("details")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = document.data
                    findViewById<TextView>(R.id.textDate).text = "From ${travelDoc["dateStart"]} to ${travelDoc["dateEnd"]}"
                    findViewById<TextView>(R.id.textDesc).text = travelDoc["description"].toString()
                    val notes = data?.get("notes") as? List<Map<String, Any>>
                    notes?.let {
                        notesList.clear()
                        for (noteMap in notes) {
                            val note = Note(
                                text = noteMap["text"] as? String ?: "",
                                imageUrl = noteMap["imageUrl"] as? String ?: ""
                            )
                            notesList.add(note)
                        }
                        notesAdapter.notifyDataSetChanged()
                    }
                }
            }
    }

    private fun showAddNoteDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_note)

        val noteEditText: EditText = dialog.findViewById(R.id.noteEditText)
        val imageView: ImageView = dialog.findViewById(R.id.imageView)
        val pickImageButton: Button = dialog.findViewById(R.id.pickImageButton)
        val addButton: Button = dialog.findViewById(R.id.addButton)

        pickImageButton.setOnClickListener {
            requestCameraPermission()
        }

        addButton.setOnClickListener {
            val noteText = noteEditText.text.toString()
            val newImageFile = File(currentPhotoPath)

            if (newImageFile != null) {
                val imageName = "${System.currentTimeMillis()}.png"
                val storageRef: StorageReference = storage.reference.child("travel_images/$imageName")
                val uploadTask = storageRef.putFile(photoUri)

                uploadTask.addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        val newNote = mapOf("text" to noteText, "imageUrl" to imageUrl)
                        notesList.add(Note(
                            text = noteText,
                            imageUrl = imageUrl
                        ))
                        saveNotes()
                        dialog.dismiss()
                    }
                }.addOnFailureListener {

                }
            } else {
                val newNote = mapOf("text" to noteText, "imageUrl" to "")
                notesList.add(Note(
                    text = noteText,
                    imageUrl = ""
                ))
                saveNotes()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                dispatchTakePictureIntent()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            null
        }
        photoFile?.also {
            photoUri = FileProvider.getUriForFile(
                this,
                "com.example.kotlinapp.fileprovider",
                it
            )
            takePictureLauncher.launch(photoUri)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    fun saveNotes() {
        val notesMapList = notesList.map { note ->
            mapOf(
                "text" to note.text,
                "imageUrl" to note.imageUrl
            )
        }
        firestore.collection("Users").document(auth.currentUser!!.uid)
            .collection("travels").document(travelId)
            .collection("details").document("details")
            .set(mapOf("notes" to notesMapList))
        loadNotes()
    }

    fun deleteNoteAtIndex(index: Int) {
        notesList.removeAt(index)
        saveNotes()
        notesAdapter.notifyItemRemoved(index)
    }
}
