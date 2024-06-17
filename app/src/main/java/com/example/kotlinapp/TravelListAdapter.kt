package com.example.kotlinapp

import android.content.Intent
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class TravelListAdapter(private val travels: List<Map<String, Any>>) :
    RecyclerView.Adapter<TravelListAdapter.ViewHolder>() {

    class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val placeTextView: TextView = itemView.findViewById(R.id.place_text_view)
        val dateTextView: TextView = itemView.findViewById(R.id.date_text_view)
        val card: CardView = itemView.findViewById(R.id.card)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_travel, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val travel = travels[position]
        holder.placeTextView.text = travel["place"] as String
        holder.dateTextView.text = "From ${travel["dateStart"]} to ${travel["dateEnd"]}"

        holder.card.setOnClickListener{
            val intent = Intent(holder.itemView.context, TravelDetails::class.java)
            intent.putExtra("TRAVEL_ID",travel["place"] as String)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return travels.size
    }
}