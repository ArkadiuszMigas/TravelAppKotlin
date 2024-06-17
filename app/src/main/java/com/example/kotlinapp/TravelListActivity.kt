package com.example.kotlinapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class TravelListActivity : ComponentActivity() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_list)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val addButton: Button= findViewById(R.id.addPlaceButton)

        firebaseFirestore.collection("Users")
            .document(firebaseAuth.currentUser!!.uid)
            .collection("travels")
            .addSnapshotListener { snapshot: QuerySnapshot?, _ ->
                if (snapshot != null) {
                    val travels = mutableListOf<Map<String, Any>>()
                    for (document: DocumentSnapshot in snapshot.documents) {
                        travels.add(document.data!!)
                    }
                    val adapter = TravelListAdapter(travels)
                    recyclerView.adapter = adapter
                }
            }
        addButton.setOnClickListener{
            val intent = Intent(this, AddTravelActivity::class.java)
            startActivity(intent)
        }
    }
}
