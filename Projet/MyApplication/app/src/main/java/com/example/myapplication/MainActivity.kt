package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var bddHelper: BDD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Initialisation de l'assistant de base de données
        bddHelper = BDD(this)
        // Récupération des références des boutons depuis le layout
        val btnPlay = findViewById<Button>(R.id.btnPlay)
        val btnScores = findViewById<Button>(R.id.btnScores)
        val btnQuit = findViewById<Button>(R.id.btnQuit)

        // Action lorsqu'on clique sur le bouton "Play"
        btnPlay.setOnClickListener {
            // Obtenir le nom du joueur via une boîte de dialogue
            getPlayerNameFromUser { name ->
                // Sauvegarder le nom du joueur dans la base de données
                bddHelper.insertPlayerName(name)

                // Lancer l'activité du jeu Sudoku en passant le nom du joueur
                val intent = Intent(this, SudokuGameActivity::class.java)
                intent.putExtra("PLAYER_NAME", name)
                startActivity(intent)
            }
        }
        // Action lorsqu'on clique sur le bouton "Scores"
        btnScores.setOnClickListener {
            // Redirection vers l'écran d'examen des scores (ScoresReviewActivity)
            val intent = Intent(this, ScoresReviewActivity::class.java)
            startActivity(intent)
        }
        // Action lorsqu'on clique sur le bouton "Quit"
        btnQuit.setOnClickListener {
            finish() // Quitter l'application
        }
    }
    // Fonction pour obtenir le nom du joueur à partir de la boîte de dialogue
    private fun getPlayerNameFromUser(callback: (String) -> Unit) {
        val playerNameDialog = AlertDialog.Builder(this)
        val editText = EditText(this)
        editText.hint = "Entrez votre nom"
        playerNameDialog.setTitle("Nom du Joueur")
        playerNameDialog.setView(editText)

        playerNameDialog.setPositiveButton("OK") { _, _ ->
            val playerName = editText.text.toString()
            callback.invoke(playerName)
        }
        playerNameDialog.setNegativeButton("Annuler") { dialog, _ ->
            dialog.cancel()
        }
        playerNameDialog.show()
    }
}
