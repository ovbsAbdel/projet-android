package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


// Adaptateur pour afficher la liste des scores dans un RecyclerView
class MyScoresAdapter(private val scores: List<SudokuScore>) :
    RecyclerView.Adapter<MyScoresAdapter.ScoreViewHolder>() {

    // Création d'une nouvelle instance de ScoreViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_scores_review, parent, false)
        return ScoreViewHolder(view)
    }

    // Liaison des données d'un score spécifique avec l'élément visuel correspondant
    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        val score = scores[position]
        holder.bind(score)
    }

    // Retourne le nombre total d'éléments dans la liste des scores
    override fun getItemCount(): Int {
        return scores.size
    }

    // Vue représentant un élément individuel de la liste des scores
    class ScoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerNameTextView: TextView = itemView.findViewById(R.id.playerNameTextView)
        private val elapsedTimeTextView: TextView = itemView.findViewById(R.id.elapsedTimeTextView)
        private val errorCountTextView: TextView = itemView.findViewById(R.id.errorCountTextView)

        // Liaison des données du score avec les éléments visuels correspondants
        fun bind(score: SudokuScore) {
            playerNameTextView.text = score.playerName
            elapsedTimeTextView.text = "Time: ${score.elapsedTime}"
            errorCountTextView.text = "Errors: ${score.errorCount}"
        }
    }
}
