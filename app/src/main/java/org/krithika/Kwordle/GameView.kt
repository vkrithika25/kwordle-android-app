package org.krithika.Kwordle

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.Toast

class GameView(private val _context: Context, private val _attrs: AttributeSet) : LinearLayout(_context, _attrs), ControlObserver {
    private val NUM_GUESSES = 6
    private lateinit var _guesses: Array<GuessView>
    private var _currentGuess: Int = 0
    private lateinit var _keyboard: Keyboard
    private var _keyboardId = 0
    private lateinit var _player1 : Player
    private lateinit var _player2 : Player
    private lateinit var _words : HashSet<String>
    private var _strictMode : Boolean = false
    private val _misplacedLetters : HashMap<Int, ArrayList<Char>> = HashMap()
    private val _notPresentLetters : HashMap<Int, ArrayList<Char>> = HashMap()
    private val _correctLetters : HashMap<Int, Char> = HashMap()

    init {
        val ta = context.obtainStyledAttributes(_attrs, R.styleable.GameView)
        _keyboardId = ta.getResourceId(R.styleable.GameView_keyboard, 0)
        ta.recycle()

        val endDivider = GradientDrawable()
        endDivider.setSize(50, 6)
        dividerDrawable = endDivider
        showDividers = (SHOW_DIVIDER_BEGINNING or SHOW_DIVIDER_MIDDLE or SHOW_DIVIDER_END)
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        _keyboard = rootView.findViewById(_keyboardId)
        _keyboard.addControlObserver(this)

        _guesses = Array(NUM_GUESSES) { GuessView(_context, _attrs, _keyboardId, _words) }
        orientation = VERTICAL
        isClickable = false
        for (i in 0 until NUM_GUESSES) {
            addView(_guesses[i], i)
        }

        for (i in 0 until NUM_GUESSES) {
            _guesses[i].setCurrentGuess(false)
            _guesses[i].setStrictMode(_strictMode)
        }
        _guesses[0].setCurrentGuess(true)

        _player2.prepareWord()
    }

    fun startGame() {
        _player1.guessWord(this, "_____")
    }

    override fun updateControl(control: ControlObserver.Control) {
        if (control == ControlObserver.Control.Enter) {
            if (_currentGuess < NUM_GUESSES) {
                if (_guesses[_currentGuess].guessIsComplete() && _guesses[_currentGuess].isWordValid()) {
                    clueModeActions()
                } else if (_guesses[_currentGuess].guessIsComplete() && !(_guesses[_currentGuess].isWordValid())){
                    val text = "That's not a word in our dictionary!"
                    val duration = Toast.LENGTH_SHORT
                    val toast = Toast.makeText(context, text, duration)
                    toast.show()
                }
            }
        } else if (control == ControlObserver.Control.ClueFinished) {
            if (_guesses[_currentGuess].isClueCompleted()) {
                if (_guesses[_currentGuess].isGuessCorrect()) {
                    val text = "That's right!"
                    val duration = Toast.LENGTH_SHORT
                    val toast = Toast.makeText(context, text, duration)
                    toast.show()

                    val i = Intent(context, StartingScreenActivity::class.java)
                    context.startActivity(i)
                } else {
                    getClueInformation()
                    moveCurrentToNextGuess()
                    passClueInformation()
                    if (_currentGuess < NUM_GUESSES) {
                        inputModeActions()
                    }
                    _guesses[_currentGuess].changeKeyboardColorsStrict()
                }
            } else {
                val text = "Please finish inputting the clue!"
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(context, text, duration)
                toast.show()
            }
        }
    }


    fun setPlayer1(player1: Player) {
        _player1 = player1
    }

    fun setPlayer2(player2: Player) {
        _player2 = player2
    }

    fun setWordList(words: HashSet<String>) {
        _words = words
    }

    fun setCurrentGuessWord(word: String) {
        if (word == "") {
            val text = "No word in our dictionary corresponds to these clues!"
            val duration = Toast.LENGTH_SHORT
            val toast = Toast.makeText(context, text, duration)
            toast.show()
            val i = Intent(context, StartingScreenActivity::class.java)
            context.startActivity(i)
        }

        var i = 0
        for (letter in word) {
            val handler = Handler()
            val r = Runnable {
                getCurrentGuessView().updateLetter(letter)
            }
            i++
            handler.postDelayed(r, (1000*i).toLong())
        }
        var r = Runnable {
            getCurrentGuessView().updateControl(ControlObserver.Control.Enter)
            updateControl(ControlObserver.Control.Enter)
        }
        i++
        handler.postDelayed(r, (1000*i).toLong())
    }

    fun setCurrentGuessClue(guess: String, clue: String) {
        var index = 0
        for (letter in clue) {
            when (ClueType.valueOf(letter)) {
                ClueType.CORRECT_CHAR -> {
                    _keyboard.updateButtonState(LetterView.Clue.Correct, guess[index])
                }
                ClueType.MISPLACED_CHAR -> {
                    _keyboard.updateButtonState(LetterView.Clue.WrongPosition, guess[index])
                }
                ClueType.WRONG_CHAR -> {
                    _keyboard.updateButtonState(LetterView.Clue.DoesNotExist, guess[index])
                }
            }
            index++
        }
        getCurrentGuessView().changeLetterStates(clue)
        _keyboard.clickDone()
    }

    private fun getCurrentGuessView(): GuessView {
        return _guesses[_currentGuess]
    }

    fun setStrictMode(strictMode: Boolean) {
        _strictMode = strictMode
    }

    private fun clueModeActions() {
        _keyboard.setKeyboardClueMode()
        _guesses[_currentGuess].setMode(GuessView.Mode.ClueMode)
        _player2.provideClue(_guesses[_currentGuess].assembleWord(), this)
        if (_guesses[_currentGuess].isClueCompleted()) {
            if (_guesses[_currentGuess].isGuessCorrect()) {
                val text = "That's correct!"
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(context, text, duration)
                toast.show()
            } else {
                moveCurrentToNextGuess()
            }
        }
    }

    private fun inputModeActions() {
        _guesses[_currentGuess].setMode(GuessView.Mode.InputMode)
        _keyboard.setKeyboardInputMode()
        _player1.guessWord(this, _guesses[_currentGuess - 1].getClueAsString())
    }

    private fun moveCurrentToNextGuess() {
        if (_currentGuess < NUM_GUESSES && _guesses[_currentGuess].guessIsComplete()) {
            _guesses[_currentGuess].setCurrentGuess(false)
            _guesses[_currentGuess].setMode(GuessView.Mode.DoneMode)
            _currentGuess++
            passClueInformation()
            _guesses[_currentGuess].changeKeyboardColorsStrict()
        }
        if (_currentGuess < NUM_GUESSES) {
            _guesses[_currentGuess].setCurrentGuess(true)

        } else {
            if (_guesses[--_currentGuess].isGuessCorrect()) {
                val text = "That's right!"
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(context, text, duration)
                toast.show()
            } else {
                val text = "That's the final guess!"
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(context, text, duration)
                toast.show()
            }
        }
    }

    private fun getClueInformation() {
        if (_strictMode) {
            val clue = _guesses[_currentGuess].getClueAsString()
            val word = _guesses[_currentGuess].assembleWord()
            var index = 0
            for (letter in clue) {
                when (letter) {
                    'G' -> _correctLetters[index] = word[index]
                    'Y' -> {
                        if (!(_notPresentLetters.containsKey(index))) {
                            _notPresentLetters[index] = ArrayList()
                        }
                        _notPresentLetters[index]?.add(word[index])
                    }
                    '_' -> {
                        for (number in 0 until 5) {
                            if (!(_notPresentLetters.containsKey(number))) {
                                _notPresentLetters[number] = ArrayList()
                            }
                            // _notPresentLetters[index]?.add(word[index])
                            /* if (_misplacedLetters[number]?.contains(word[index]) == true) {
                                if (!(_notPresentLetters.containsKey(number))) {
                                    _notPresentLetters[number] = ArrayList()
                                }
                                _notPresentLetters[number]?.add(word[index])
                            } */
                            _notPresentLetters[number]?.add(word[index])
                        }
                    }
                }
                index++
            }
        }
    }

    private fun passClueInformation() {
        if (_strictMode) {
            _guesses[_currentGuess].setCorrectLetters(_correctLetters)
            _guesses[_currentGuess].setMisplacedLetters(_misplacedLetters)
            _guesses[_currentGuess].setNotPresentLetters(_notPresentLetters)
        }
    }
}
