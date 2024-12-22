package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

// Classe SudokuGameActivity défini toutes les règle du jeu Sudoku
class SudokuGameActivity : AppCompatActivity(), SudokuEventListener {

    // Listes de variables pour le suivi des scores et de l'état du jeu
    //lateinit permet de déclarer la variables que lorsqu'elle est appelée
    private val scoresList: MutableList<SudokuScore> = mutableListOf()
    private var currentScore: Int = 0
    private var errorCount: Int = 0
    private lateinit var simpleChronometer: Chronometer
    private lateinit var bddHelper: BDD
    private lateinit var playerName: String
    private lateinit var grilleSudokuView: GrilleSudoku
    private lateinit var recyclerView: RecyclerView
    private lateinit var textViewLastScore: TextView
    private lateinit var textViewScores: TextView
    private var isSudokuLayout: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playerName = intent.getStringExtra("PLAYER_NAME") ?: "Player"

        // Détermine la disposition de l'activité en fonction de la variable isSudokuLayout.
        if (isSudokuLayout) {
            setContentView(R.layout.activity_sudoku_game)
            initializeSudokuGameLayout()
        } else {
            setContentView(R.layout.activity_scores_review)
            initializeScoresReviewLayout()
        }
    }

    // Logique de navigation vers le menu principal après la partie.
    override fun navigateToMainMenu(score: Int) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("lastScore", score)
        startActivity(intent)
        finish() // Optionnel : Termine cette activité si vous souhaitez retourner au menu principal.
    }

    // Initialisation de la disposition du jeu Sudoku.
    private fun initializeSudokuGameLayout() {
        grilleSudokuView = findViewById(R.id.grilleSudoku)
        grilleSudokuView.setSudokuEventListener(this)
        textViewScores = findViewById(R.id.textViewScores)
        bddHelper = BDD(this)

        // Bouton de retour à la page d'accueil.
        val btnBack = findViewById<ImageView>(R.id.btnBackToHome)
        btnBack.setOnClickListener {
            finish()
        }

        // Bouton de démarrage d'une nouvelle partie.
        val btnNewGame = findViewById<ImageView>(R.id.btnNewGame)
        btnNewGame.setOnClickListener {
            grilleSudokuView.newGame()
            grilleSudokuView.invalidate()
            startTimer()
        }

        // Autres initialisations spécifiques à la disposition du Sudoku.
        startTimer()
    }

    // Initialisation de la disposition de la revue des scores.
    private fun initializeScoresReviewLayout() {
        recyclerView = findViewById(R.id.recyclerView)
        textViewLastScore = findViewById(R.id.textViewLastScore)
        bddHelper = BDD(this)

        // Adapter pour afficher la liste des scores.
        val adapter = MyScoresAdapter(scoresList)
        recyclerView.adapter = adapter

        // Autres initialisations spécifiques à la disposition de la revue des scores.
    }

    // Affichage du score actuel.
    private fun displayScores(score: Int) {
        textViewScores.text = "Score: $score"
    }

    // Gestion de l'augmentation du score lors de la saisie correcte d'un chiffre.
    private fun handleCorrectNumberInput() {
        currentScore += 20 // Augmente le score de 20 (à ajuster en fonction du système de notation).
        displayScores(currentScore) // Met à jour l'affichage du score.
    }

    // Démarrage du chronomètre.
    private fun startTimer() {
        simpleChronometer = findViewById(R.id.simpleChronometer)
        simpleChronometer.base = SystemClock.elapsedRealtime()
        simpleChronometer.start()
    }

    // Arrêt du chronomètre.
    private fun stopTimer() {
        simpleChronometer.stop()
    }

    // Sauvegarde du score dans la base de données.
    private fun saveScoreToDatabase() {
        val elapsedTime = SystemClock.elapsedRealtime() - simpleChronometer.base
        bddHelper.addScore(playerName, elapsedTime, errorCount, currentScore)
    }

    // Actions à effectuer lorsque le jeu se termine.
    private fun onGameFinish() {
        stopTimer()
        saveScoreToDatabase()
        scoresList.addAll(bddHelper.getAllScores())
        displayLastScoreInScoreTab()
    }

    // Affichage du dernier score dans l'onglet des scores.
    private fun displayLastScoreInScoreTab() {
        val lastScore = getLastScoreForPlayer(playerName)
        if (lastScore != null) {
            textViewLastScore.text =
                "Last Score: ${lastScore.elapsedTime} - Errors: ${lastScore.errorCount}"
        } else {
            textViewLastScore.text = "No previous score available"
        }
    }

    // Récupération du dernier score pour un joueur spécifique.
    private fun getLastScoreForPlayer(playerName: String): SudokuScore? {
        return scoresList.lastOrNull { it.playerName == playerName }
    }

    // Gestion de la saisie correcte d'un chiffre.
    override fun onCorrectNumberEntered() {
        handleCorrectNumberInput()
    }

    // Actions à effectuer lors de la destruction de l'activité.
    override fun onDestroy() {
        super.onDestroy()
        onGameFinish()
    }
}
