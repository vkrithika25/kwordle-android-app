package org.krithika.Kwordle

interface Player {
    enum class Type {
        HumanPlayer,
        ComputerPlayer
    }

    fun prepareWord()
    fun guessWord(gameView: GameView, clue : String)
    fun provideClue(guess: String, gameView: GameView)
}