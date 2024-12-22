package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

/**
 * Activité responsable de l'affichage des scores des parties précédentes et du dernier score pour un joueur donné.
 * Elle utilise une base de données SQLite pour stocker et récupérer les données des scores.
 */
class ScoresReviewActivity : AppCompatActivity() {
    private lateinit var bddHelper: BDD
    private lateinit var scoresList: List<SudokuScore>
    private lateinit var textViewLastScore: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scores_review)

        // Initialisation des éléments de l'interface utilisateur
        textViewLastScore = findViewById(R.id.textViewLastScore)
        bddHelper = BDD(this)
        scoresList = getAllScoresFromDatabase()

        // Affiche le dernier score pour le joueur spécifié
        displayLastScoreForPlayer("PlayerName")

        // Configure le RecyclerView pour afficher la liste des scores
        setupRecyclerView()

        // Configure le bouton de retour au menu principal
        setupBackToMenuButton()
    }

    // Récupère tous les scores depuis la base de données
    private fun getAllScoresFromDatabase(): List<SudokuScore> {
        return bddHelper.getAllScores()
    }

    // Affiche le dernier score d'un joueur spécifique
    private fun displayLastScoreForPlayer(playerName: String) {
        val lastScore = getLastScoreForPlayer(playerName)
        if (lastScore != null) {
            textViewLastScore.text =
                "Dernier Score : ${lastScore.elapsedTime} - Erreurs : ${lastScore.errorCount}"
        } else {
            textViewLastScore.text = "Aucun score précédent disponible"
        }
    }

    // Récupère le dernier score d'un joueur spécifique depuis la liste des scores
    private fun getLastScoreForPlayer(playerName: String): SudokuScore? {
        return scoresList.filter { it.playerName == playerName }.lastOrNull()
    }

    // Configure le RecyclerView pour afficher la liste des scores
    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = MyScoresAdapter(scoresList)
    }

    // Configure le bouton de retour au menu principal
    private fun setupBackToMenuButton() {
        val btnBackToMenu = findViewById<Button>(R.id.btnBackToMenu)
        btnBackToMenu.setOnClickListener {
            // Retourne au menu principal en créant une nouvelle activité MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }
    }
}


