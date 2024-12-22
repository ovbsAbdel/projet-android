package com.example.myapplication

//Interface définissant les événements Sudoku auxquels l'activité principale doit répondre.

/*onCorrectNumberEntered() : On apelle cette méthode lorsqu'un chiffre correct est saisi dans la grille Sudoku
    Cela est utilisé pour gérer les actions associées à une entrée correcte, telles que la mise à jour du score.
     */
/*navigateToMainMenu : On apelle cette méthode pour naviguer vers le menu principal (Sudoku vers menu principal) avec le score fourni
     Cela permet donc de transmettre les informations sur le score actuel
     */
interface SudokuEventListener {
    fun onCorrectNumberEntered()
    fun navigateToMainMenu(score: Int)
}
