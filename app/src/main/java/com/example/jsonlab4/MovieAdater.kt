package com.example.jsonlab4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MovieAdapter(
    private var items: List<Movie>
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitleRating: TextView = itemView.findViewById(android.R.id.text1)
        val tvDetails: TextView = itemView.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = items[position]
        holder.tvTitleRating.text = "${movie.title} — рейтинг: ${movie.rating}"
        holder.tvDetails.text = "Режиссёр: ${movie.director.name} | Жанров: ${movie.genres.size}"
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<Movie>) {
        items = newItems
        notifyDataSetChanged()
    }
}
