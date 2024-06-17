package com.example.kotlinapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class AddTravelActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var placeEditText: EditText
    private lateinit var dateStartEditText: EditText
    private lateinit var dateEndEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var addTravelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_travel)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        placeEditText = findViewById(R.id.placeEditText)
        dateStartEditText = findViewById(R.id.dateStartEditText)
        dateEndEditText = findViewById(R.id.dateEndEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        addTravelButton = findViewById(R.id.addTravelButton)

        dateStartEditText.setOnClickListener { selectStartDate() }
        dateEndEditText.setOnClickListener { selectEndDate() }
        addTravelButton.setOnClickListener { uploadData() }
    }

    private fun uploadData() {
        val place = placeEditText.text.toString()
        val dateStart = dateStartEditText.text.toString()
        val dateEnd = dateEndEditText.text.toString()
        val description = descriptionEditText.text.toString()

        if (place.isEmpty() || dateStart.isEmpty() || dateEnd.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val travelData = hashMapOf(
            "place" to place,
            "dateStart" to dateStart,
            "dateEnd" to dateEnd,
            "description" to description
        )

        firestore.collection("Users").document(auth.currentUser!!.uid)
            .collection("travels").document(place)
            .set(travelData)
            .addOnSuccessListener {
                Toast.makeText(this, "Travel added successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add travel: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun selectStartDate() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            dateStartEditText.setText("$selectedYear-${selectedMonth + 1}-$selectedDay")
        }, year, month, day)
        datePickerDialog.show()
    }

    private fun selectEndDate() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            dateEndEditText.setText("$selectedYear-${selectedMonth + 1}-$selectedDay")
        }, year, month, day)
        datePickerDialog.show()
    }

}
