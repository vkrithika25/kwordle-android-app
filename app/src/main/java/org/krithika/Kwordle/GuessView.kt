package org.krithika.Kwordle

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.Toast

class GuessView(
    _context: Context,
    _attrs: AttributeSet,
    private val _keyboardId: Int,
    private val _words: HashSet<String>
) : LinearLayout(_context, _attrs), LetterObserver, ControlObserver {
    private val _letters: Array<LetterView>
    private var _currentLetter: Int = 0
    private var _keyboard: Keyboard? = null
    private var NUM_LETTERS = 5
    private var _guessMode = Mode.InputMode
    private var _isCurrentGuess = false
    private var _misplacedLetters : HashMap<Int, ArrayList<Char>> = HashMap()
    private var _notPresentLetters : HashMap<Int, ArrayList<Char>> = HashMap()
    private var _correctLetters : HashMap<Int, Char> = HashMap()
    private var _strictMode = false

    enum class Mode {
        InputMode,
        ClueMode,
        DoneMode
    }


    init {
        _letters = Array(5) { LetterView(_context, _attrs, _keyboardId)}

        val backgroundDrawable = GradientDrawable()
        backgroundDrawable.setStroke(6, resources.getColor(R.color.GuessViewStroke))
        backgroundDrawable.setColor(resources.getColor(R.color.GuessViewBackground))
        background = backgroundDrawable

        val endDivider = GradientDrawable()
        endDivider.setSize(50, 1)
        dividerDrawable = endDivider
        showDividers = (SHOW_DIVIDER_BEGINNING or SHOW_DIVIDER_MIDDLE or SHOW_DIVIDER_END)

        orientation = HORIZONTAL
        isClickable = false
        for (i in 0 until NUM_LETTERS) {
            addView(_letters[i], i)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        _keyboard = rootView.findViewById(_keyboardId)

        for (i in 0 until NUM_LETTERS) {
            _letters[i].setMode(LetterView.LetterMode.InputMode)
            _letters[i].setIsActiveInput(false)
        }

        _letters[0].setIsActiveInput(true)
        setCurrentGuess(_isCurrentGuess) // just to set up observers
    }

    // only for human guesser
    override fun updateLetter(ch: Char) {
        if (_currentLetter < NUM_LETTERS) {
            _letters[_currentLetter].updateLetter(ch)
            moveCurrentToNextLetter()
        }
    }


    // guessview can keep this responsibility
    override fun updateControl(control: ControlObserver.Control) {
        if (control == ControlObserver.Control.Delete && _guessMode != Mode.ClueMode) {
            moveCurrentToPreviousLetter()
            _letters[_currentLetter].clearLetter()
            changeKeyboardColorsStrict()
        } else if (control == ControlObserver.Control.Enter && guessIsComplete() && isWordValid()) {
            _letters[_currentLetter - 1].setIsActiveInput(false)
        } else if (!guessIsComplete()) {
            val text = "Please finish inputting your guess!"
            val duration = Toast.LENGTH_SHORT
            val toast = Toast.makeText(context, text, duration)
            toast.show()
        }
    }

    // potentially make this a player function? but player should not be changing the letterview to input though
    fun setMode(mode: Mode) {
        _guessMode = mode
        when (mode) {
            Mode.InputMode -> inputModeActions()
            Mode.ClueMode -> clueModeActions()
            Mode.DoneMode -> doneModeActions()
        }
    }

    fun setCurrentGuess(bool: Boolean) {
        _isCurrentGuess = bool
        if (_isCurrentGuess) {
            currentGuessActions()
        } else {
            notCurrentGuessActions()
        }

    }

    fun guessIsComplete(): Boolean {
        return isCurrentAtPhantomLastLetter()
    }

    fun isClueCompleted() : Boolean {
        for (letters in _letters) {
            if (letters.getClue() == LetterView.Clue.Unknown) {
                return false
            }
        }
        return true
    }


    fun assembleWord() : String {
        val inputString : StringBuilder = StringBuilder()
        for (i in 0 until NUM_LETTERS) {
            inputString.append(_letters[i].getLetter())
        }
        return inputString.toString()
    }

    fun assembleClue() {
        for (letters in _letters) {
            _keyboard?.updateButtonState(letters.getClue(), letters.getLetter())
        }
    }

     fun isGuessCorrect(): Boolean {
        for (letters in _letters) {
            if (letters.getClue() != LetterView.Clue.Correct) {
                return false
            }
        }
        return true
    }


     fun isWordValid(): Boolean {
        val guess = assembleWord()
        for (words in _words) {
            if (guess.toLowerCase() == words) {
                return true
            }
        }
        return false
    }

    fun getClueAsString() : String {
        val clue : StringBuilder = StringBuilder("")
        for (letter in _letters) {
            when(letter.getClue()) {
                LetterView.Clue.Correct -> clue.append('G')
                LetterView.Clue.WrongPosition -> clue.append('Y')
                else -> clue.append('_')
            }
        }
        return clue.toString()
    }

    fun changeLetterStates(clue: String) {
        var index = 0
        for (letters in clue) {
            when (letters) {
                'G' -> _letters[index].setClue(LetterView.Clue.Correct)
                'Y' -> _letters[index].setClue(LetterView.Clue.WrongPosition)
                else -> _letters[index].setClue(LetterView.Clue.DoesNotExist)
            }
            index++
        }
    }

    fun setCorrectLetters(correctLetters: HashMap<Int, Char>) {
        _correctLetters = correctLetters
    }

    fun setMisplacedLetters(misplacedLetters: HashMap<Int, ArrayList<Char>>) {
        _misplacedLetters = misplacedLetters
    }

    fun setNotPresentLetters(notPresentLetters: HashMap<Int, ArrayList<Char>>) {
        _notPresentLetters = notPresentLetters
    }

    fun setStrictMode(strictMode: Boolean) {
        _strictMode = strictMode
    }

    fun changeKeyboardColorsStrict() {
        if (_guessMode == Mode.ClueMode) {
            _keyboard?.greyOutButtons()
        } else if (_strictMode) {
            _keyboard?.setupListeners()
            if (_correctLetters.containsKey(_currentLetter)) {
                for (ch in 'A'..'Z') {
                    _keyboard?.disableButton(ch)
                }
                _keyboard?.enableButton(_correctLetters[_currentLetter], Keyboard.State.Correct)
            } else {
                if (_notPresentLetters.containsKey(_currentLetter)) {
                    for (letter in _notPresentLetters[_currentLetter]!!) {
                        _keyboard?.disableButton(letter)
                    }
                }
                if (_misplacedLetters.containsKey(_currentLetter)) {
                    for (letter in _misplacedLetters[_currentLetter]!!) {
                        _keyboard?.enableButton(letter, Keyboard.State.WrongPosition)
                    }
                }
            }
        }
    }

    private fun currentGuessActions() {
        _keyboard?.addLetterObserver(this)
        _keyboard?.addControlObserver(this)
        _letters[0].setIsActiveInput(true)
    }

    private fun notCurrentGuessActions() {
        _keyboard?.removeLetterObserver(this)
        _keyboard?.removeControlObserver(this)
        _letters[0].setIsActiveInput(false)
    }

    private fun isCurrentAtFirstLetter(): Boolean {
        return _currentLetter == 0
    }

    private fun isCurrentAtLastLetter(): Boolean {
        return _currentLetter == NUM_LETTERS - 1
    }

    private fun isCurrentAtPhantomLastLetter(): Boolean {
        return _currentLetter == NUM_LETTERS
    }

    private fun moveCurrentToPreviousLetter() {
        if (!isCurrentAtFirstLetter()) {
            if (!isCurrentAtPhantomLastLetter()) {
                _letters[_currentLetter].setIsActiveInput(false)
            }
            _currentLetter--
            _letters[_currentLetter].setIsActiveInput(true)
        }
    }

    private fun moveCurrentToNextLetter() {
        if (!isCurrentAtLastLetter()) {
            _letters[_currentLetter].setIsActiveInput(false)
            _currentLetter++
            _letters[_currentLetter].setIsActiveInput(true)
            changeKeyboardColorsStrict()
        } else {
            changeKeyboardColorsStrict()
            _currentLetter++ // Move the current letter to a "phantom" last letter that does not exist
        }
    }
    private fun inputModeActions() {
        for (letter in _letters) {
            letter.setMode(LetterView.LetterMode.InputMode)
        }
    }

    private fun clueModeActions() {
        // this is what needs to happen for human giving clue
        for (letter in _letters) {
            letter.setMode(LetterView.LetterMode.ClueMode)
        }
    }

    private fun doneModeActions() {
        for (letter in _letters) {
            letter.setMode(LetterView.LetterMode.DoneMode)
        }
    }

}
