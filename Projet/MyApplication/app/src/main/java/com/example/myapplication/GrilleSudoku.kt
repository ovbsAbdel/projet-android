package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class GrilleSudoku(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    //Déclaration de variables
    private var sudokuEventListener: SudokuEventListener? = null //Ecoute de la saisie d'un nombre
    private lateinit var initialGrid: Array<IntArray> // Grille de départ évite à l'utilisateur d'entrée des valeur dans les case randomiser
    private var scoreTextView: TextView? = null //Affiche Le score
    private val smallLinePaint: Paint = Paint() // Peinture des lignes fines de la grille
    private val bigLinePaint: Paint = Paint() // Peinture des lignes épaisses de la grille
    private val textPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG) //Peinture pour les chiffre
    private val userTextPaint: Paint =
        Paint(Paint.ANTI_ALIAS_FLAG) //Peinture pour les chiffres de l'utilisateur
    private var grilleSudoku =
        Array(9) { IntArray(9) } // Grille représentant l'état actuel du Sudoku
    private var selectedRow = -1 // Position des lignes
    private var selectedColumn = -1 // Position des colonnes
    private var errorCount = 0 // Compteur d'erreurs de l'utilisateur
    private var currentScore = 0 // Score actuel de l'utilisateur
    private var correctInputs = 0// Score saisies correct de l'utilisateur

    //Initialise la grille
    init {
        setupPaints() // Configuration initiales des peintures
        initializeGrid() // Initialisation de la grille
    }

    //Méthode pour la couleur des différents lignes et du texte
    private fun setupPaints() {
        //Couleur petites lignes
        smallLinePaint.apply {
            strokeWidth = 2f
            color = resources.getColor(R.color.greenfonce)
        }
        //Couleur grosse lignes
        bigLinePaint.apply {
            strokeWidth = 8f
            color = resources.getColor(R.color.test)
        }
        //Couleur du texte
        textPaint.apply {
            textSize = 40f
            color = resources.getColor(R.color.black)
            textAlign = Paint.Align.CENTER
        }
        //Couleur texte utilisateur
        userTextPaint.apply {
            textSize = 40f
            color = resources.getColor(R.color.bleufonce)
            textAlign = Paint.Align.CENTER
        }
    }

    //Ecoute des évenements du Sudoku
    fun setSudokuEventListener(listener: SudokuEventListener) {
        sudokuEventListener = listener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //Calcul des dimmensions de la vue
        val width = width.toFloat()
        val height = height.toFloat()


        //Permet de dessiner les lignes épaisses séparant les blocs  3 6 9| 6 4 0 | 0 9 4 |
        for (i in 1 until 3) {
            val x = i * width / 3f
            canvas.drawLine(x, 0f, x, height, bigLinePaint)
        }

        for (i in 1 until 3) {
            val y = i * height / 3f
            canvas.drawLine(0f, y, width, y, bigLinePaint)
        }
        //Permet de dessiner les lignes fines séparant les cellules
        for (i in 1 until 9) {
            val x = i * width / 9f
            canvas.drawLine(x, 0f, x, height, smallLinePaint)
        }

        for (i in 1 until 9) {
            val y = i * height / 9f
            canvas.drawLine(0f, y, width, y, smallLinePaint)
        }

        //Dessin des valeurs des cellules et des rectangles de sélection
        for (i in 0 until 9) {
            for (j in 0 until 9) {
                val cellValue = grilleSudoku[i][j]
                val x = j * width / 9f + width / 18f
                val y = i * height / 9f + height / 18f

                // Dessin de la valeur de la cellule
                if (cellValue != 0) {
                    canvas.drawText(cellValue.toString(), x, y, textPaint)
                } else if (i == selectedRow && j == selectedColumn) {
                    //Dessin du rectangle de sélection pour la cellule active
                    canvas.drawRect(
                        j * width / 9f,
                        i * height / 9f,
                        (j + 1) * width / 9f,
                        (i + 1) * height / 9f,
                        bigLinePaint
                    )
                }
            }
        }
    }

    // Gestion de l'événement tactile lorsqu'un utilisateur touche l'écran
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            // Récupérer les coordonnées du toucher
            val x = event.x.toInt()
            val y = event.y.toInt()

            // Calculer la ligne et la colonne de la cellule sélectionnée
            selectedRow = (y / (height / 9)).coerceIn(0, 8)
            selectedColumn = (x / (width / 9)).coerceIn(0, 8)

            // Vérifier si la cellule est vide, si oui, afficher le dialogue de saisie des chiffres
            if (grilleSudoku[selectedRow][selectedColumn] == 0) {
                showNumberInputDialog()
            }

            // Forcer le redessin de la vue et vérifier l'état du Sudoku
            invalidate()
            checkSudoku()

            return true
        }
        return false
    }

    // Définir le TextView pour afficher le score et mettre à jour le texte du score initialement
    fun setScoreTextView(textView: TextView) {
        scoreTextView = textView
        updateScoreText()
    }

    // Mettre à jour le texte du score affiché dans le TextView
    private fun updateScoreText() {
        scoreTextView?.text = "Score: $currentScore"
    }

    // Calculer le bonus de suppression de section complète et mettre à jour le score
    private fun calculateSectionClearingBonus(): Int {
        var sectionBonus = 0

        // Vérifier les lignes horizontales et verticales complètes
        for (i in 0 until 9) {
            if (isHorizontalLineComplete(i)) {
                sectionBonus += 200
            }
            if (isVerticalLineComplete(i)) {
                sectionBonus += 200
            }
        }

        // Vérifier les sections 3x3 complètes
        for (i in 0 until 9 step 3) {
            for (j in 0 until 9 step 3) {
                if (is3x3AreaComplete(i, j)) {
                    sectionBonus += 200
                }
            }
        }

        // Ajouter le bonus à la note actuelle, forcer le redessin de la vue et retourner le bonus
        currentScore += sectionBonus
        invalidate()

        return sectionBonus
    }

    // Gestion des actions après la saisie d'un chiffre correct dans la grille
    private fun onCorrectNumberEntered() {
        // Notifier l'événement de Sudoku sur une saisie correcte
        sudokuEventListener?.onCorrectNumberEntered()

        // Incrémenter le score de 20 points lorsque le chiffre deviné est correct
        currentScore += 20
        correctInputs++

        // Log pour déboguer les informations sur le score et les entrées correctes
        Log.d(
            TAG,
            "Correct input detected. Current score: $currentScore, Correct inputs: $correctInputs"
        )

        // Vérifier si le nombre d'entrées correctes est supérieur ou égal à 5
        if (correctInputs >= 5) {
            // Ajouter un bonus supplémentaire de 20 points après 5 entrées correctes
            currentScore += 20
            correctInputs = 0
            Log.d(TAG, "Bonus awarded. Updated score: $currentScore")
        }

        // Calculer le bonus de suppression de section complète et forcer le redessin de la vue
        calculateSectionClearingBonus()
        invalidate()

        // Ajouter un log pour vérifier la valeur du score après chaque mise à jour
        updateScoreText()
        Log.d(TAG, "Current score after updating: $currentScore")
    }

    // Afficher une boîte de dialogue pour la saisie du chiffre dans la cellule sélectionnée
    private fun showNumberInputDialog() {
        val editText = EditText(context)
        editText.inputType = InputType.TYPE_CLASS_NUMBER

        // Créer une boîte de dialogue avec un champ d'édition pour saisir le chiffre
        val dialog = AlertDialog.Builder(context)
            .setTitle("Saisir un chiffre")
            .setMessage("Saisissez un chiffre pour la case sélectionnée:")
            .setView(editText)
            .setPositiveButton("OK") { _, _ ->
                val input = editText.text.toString()
                if (input.isNotEmpty()) {
                    val number = input.toIntOrNull()
                    if (number != null && number in 1..9) {
                        if (isValidMove(selectedRow, selectedColumn, number)) {
                            grilleSudoku[selectedRow][selectedColumn] = number
                            onCorrectNumberEntered()
                            checkSudoku()
                        } else {
                            errorCount++
                            if (errorCount == 3) {
                                showErrorDialog()
                            }
                        }
                    } else {
                        // Incrémenter le compteur d'erreurs et afficher une boîte de dialogue d'erreur après 3 erreurs
                        errorCount++
                        if (errorCount == 3) {
                            showErrorDialog()
                        }
                    }
                }
            }
            .setNegativeButton("Annuler", null)
            .create()

        dialog.show()
    }


    // Gestion de l'action après la saisie correcte d'un chiffre
    private fun handleCorrectNumberInput() {
        onCorrectNumberEntered()
    }

    // Vérifie si une ligne horizontale est complète (tous les chiffres sont remplis)
    private fun isHorizontalLineComplete(rowIndex: Int): Boolean {
        for (col in 0 until 9) {
            if (grilleSudoku[rowIndex][col] == 0) {
                return false
            }
        }
        return true
    }

    // Vérifie si une colonne verticale est complète (tous les chiffres sont remplis)
    private fun isVerticalLineComplete(colIndex: Int): Boolean {
        for (row in 0 until 9) {
            if (grilleSudoku[row][colIndex] == 0) {
                return false
            }
        }
        return true
    }

    // Vérifie si une zone 3x3 est complète (tous les chiffres sont remplis)
    private fun is3x3AreaComplete(startRow: Int, startCol: Int): Boolean {
        for (i in startRow until startRow + 3) {
            for (j in startCol until startCol + 3) {
                if (grilleSudoku[i][j] == 0) {
                    return false
                }
            }
        }
        return true
    }

    // Vérifie si la configuration actuelle du Sudoku est correcte
    private fun isSudokuCorrect(): Boolean {
        for (i in 0 until 9) {
            for (j in 0 until 9) {
                val currentCellValue = grilleSudoku[i][j]
                val initialCellValue = initialGrid[i][j]

                // Vérifie si la cellule actuelle est remplie et si elle ne correspond pas à la valeur initiale
                if (currentCellValue != 0 && initialCellValue != 0 && currentCellValue != initialCellValue) {
                    Log.d(
                        TAG,
                        "Mismatch at cell [$i][$j]. Current value: $currentCellValue, Initial value: $initialCellValue"
                    )
                    return false
                }
            }
        }
        return true
    }

    // Vérifie si le Sudoku est entièrement rempli
    private fun isSudokuComplete(): Boolean {
        for (i in 0 until 9) {
            for (j in 0 until 9) {
                if (grilleSudoku[i][j] == 0) {
                    return false
                }
            }
        }
        return true
    }


    // Vérifie l'état du Sudoku
    private fun checkSudoku() {
        Log.d(TAG, "Vérification du Sudoku en cours...")
        if (isSudokuComplete()) {
            Log.d(TAG, "Sudoku complet")
            if (isSudokuCorrect()) {
                Log.d(TAG, "Sudoku correct !")
                showSuccessDialog()
            } else {
                Log.d(TAG, "Sudoku incorrect...")
                showErrorDialog()
            }
        } else {
            Log.d(TAG, "Sudoku incomplet...")
        }
    }

    // Vérifie si une cellule est vide dans la grille
    private fun isAnyCellEmpty(): Boolean {
        for (i in 0 until 9) {
            for (j in 0 until 9) {
                if (grilleSudoku[i][j] == 0) {
                    return true
                }
            }
        }
        return false
    }

    // Initialise la grille du Sudoku
    private fun initializeGrid() {
        grilleSudoku = Array(9) { IntArray(9) }
        fillSudokuGrid()
        clearRandomCells()
        selectedRow = -1
        selectedColumn = -1
        errorCount = 0
        currentScore = 0
        correctInputs = 0

        // Initialise initialGrid avec les mêmes valeurs que grilleSudoku
        initialGrid = Array(9) { row -> grilleSudoku[row].clone() }

        invalidate()
    }

    // Remplit la grille du Sudoku avec des valeurs
    private fun fillSudokuGrid() {
        fillValues(0, 0)
        initialGrid = Array(9) { row -> grilleSudoku[row].clone() }
    }

    // Remplit récursivement les valeurs dans la grille du Sudoku
    private fun fillValues(row: Int, col: Int): Boolean {
        if (row == 9) {
            if (isSudokuComplete()) {
                onCorrectNumberEntered()
            }
            return true
        }

        val nextRow = if (col == 8) row + 1 else row
        val nextCol = if (col == 8) 0 else col + 1

        val values = (1..9).shuffled().toIntArray()

        for (value in values) {
            if (isValidMove(row, col, value)) {
                grilleSudoku[row][col] = value

                if (fillValues(nextRow, nextCol)) {
                    return true
                }
                grilleSudoku[row][col] = 0
            }
        }

        return false
    }


    // Vérifie si le mouvement est valide dans la grille du Sudoku

    private fun isValidMove(row: Int, col: Int, value: Int): Boolean {
        for (i in 0 until 9) {
            if (grilleSudoku[row][i] == value || grilleSudoku[i][col] == value) {
                return false
            }
        }

        val startRow = row - row % 3
        val startCol = col - col % 3
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (grilleSudoku[i + startRow][j + startCol] == value) {
                    return false
                }
            }
        }
        return true
    }

    //Affiche d'une boîte de dialogue d'erreur en cas de défaite (3 erreur max)

    private fun showErrorDialog() {
        AlertDialog.Builder(context)
            .setTitle("Vous avez perdu!")
            .setMessage("Vous avez entré trois fois le mauvais chiffre. Un nouveau sudoku a été généré.")
            .setPositiveButton("OK") { _, _ ->
                saveScoreAndNavigateBack()
            }
            .setCancelable(false)
            .show()
    }

    //Affiche une boîte de dialogue de succès en cas de victoire

    private fun showSuccessDialog() {
        AlertDialog.Builder(context)
            .setTitle("Félicitations!")
            .setMessage("Vous avez résolu le Sudoku avec succès.")
            .setPositiveButton("OK") { _, _ ->
                saveScoreAndNavigateBack()
            }
            .setCancelable(false)
            .show()
    }

    //Sauvegarde le score et retourne au menu principal

    private fun saveScoreAndNavigateBack() {
        val lastScore = currentScore
        newGame()
        sudokuEventListener?.navigateToMainMenu(lastScore)
    }

    //Efface un nombre aléatoire de cellules dans la grille

    private fun clearRandomCells() {
        val cellsToClearPerSubgrid = 1

        for (subgridRow in 0 until 3) {
            for (subgridCol in 0 until 3) {
                val subgridCells = mutableListOf<Pair<Int, Int>>()

                for (i in 0 until 3) {
                    for (j in 0 until 3) {
                        subgridCells.add(Pair(subgridRow * 3 + i, subgridCol * 3 + j))
                    }
                }

                subgridCells.remove(Pair(0, 0))

                subgridCells.shuffle()

                for (k in 0 until cellsToClearPerSubgrid) {
                    val cell = subgridCells[k]
                    grilleSudoku[cell.first][cell.second] = 0
                }
            }
        }
    }

    //Initialise un nouveau jeu de Sudoku

    fun newGame() {
        initializeGrid()
        invalidate()
    }

    companion object {
        private const val TAG = "GrilleSudoku"
    }
}