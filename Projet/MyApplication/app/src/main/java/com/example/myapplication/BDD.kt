package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Classe BDD représentant un gestionnaire de base de données SQLite pour stocker les scores Sudoku
class BDD(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        companion object {
        const val DATABASE_NAME = "SudokuScores.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "sudoku_scores"
        const val COLUMN_ID = "id"
        const val COLUMN_PLAYER_NAME = "player_name"
        const val COLUMN_ELAPSED_TIME = "elapsed_time"
        const val COLUMN_ERROR_COUNT = "error_count"
        const val COLUMN_SCORE_COUNT = "scores"
    }
    // Schéma de la table des données
    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_PLAYER_NAME TEXT," +
                "$COLUMN_ELAPSED_TIME INTEGER," +
                "$COLUMN_ERROR_COUNT INTEGER,"+
                "$COLUMN_SCORE_COUNT INTEGER)"
        db?.execSQL(createTableQuery)
    }

    // Mise à niveau de la base de données lorsque c'est nécessaire
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
    // Ajoute un score à la base de données Retourne l'ID du score ajouté
    fun addScore(playerName: String, elapsedTime: Long, errorCount: Int, score: Int): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PLAYER_NAME, playerName)
            put(COLUMN_ELAPSED_TIME, elapsedTime)
            put(COLUMN_ERROR_COUNT, errorCount)
            put(COLUMN_SCORE_COUNT, score)
        }
        return db.insert(TABLE_NAME, null, values)
    }
    // Récupère tous les scores de la base de données. Retourne une liste des scores Sudoku enregistrés
    fun getAllScores(): List<SudokuScore> {
        val scores = mutableListOf<SudokuScore>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        cursor?.use {
            while (it.moveToNext()) {
                val scoreId = it.getInt(it.getColumnIndex(COLUMN_ID))
                val playerName = it.getString(it.getColumnIndex(COLUMN_PLAYER_NAME))
                val elapsedTime = it.getLong(it.getColumnIndex(COLUMN_ELAPSED_TIME))
                val errorCount = it.getInt(it.getColumnIndex(COLUMN_ERROR_COUNT))
                scores.add(SudokuScore(scoreId, playerName, elapsedTime, errorCount))
            }
        }
        cursor.close()
        return scores
    }

    // Insère le nom d'un joueur dans la base de données. Retourne l'ID du joueur inséré
    fun insertPlayerName(playerName: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PLAYER_NAME, playerName)
        }
        return db.insert(TABLE_NAME, null, values)
    }
}
