package com.example.myapplication

//Classe de SudokuScore permettant de les enregistré dans la base de données
data class SudokuScore(
    val id: Int,
    val playerName: String,
    val elapsedTime: Long,
    val errorCount: Int,
)