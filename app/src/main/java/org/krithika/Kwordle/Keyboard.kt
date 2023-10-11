package org.krithika.Kwordle

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout

class Keyboard internal constructor(
    _context : Context,
    _attrs : AttributeSet
) : Observable, ConstraintLayout(_context, _attrs) {

    enum class State(hierarchy : Int) {
        Unknown(0),
        DoesNotExist(1),
        WrongPosition(2),
        Correct(3)
    }

    override val letterObservers : HashSet<LetterObserver> = HashSet()
    override val controlObservers : HashSet<ControlObserver> = HashSet()
    private val buttonState : HashMap<Char, State> = HashMap()
    private val buttonLetter : HashMap<Char, Button> = HashMap()

    init {
        inflate(context, R.layout.keyboard, this)
        getViewById(R.id.ClueModeSetter).visibility = GONE
        initializeButtonMaps()
    }

    fun acceptInput(bool : Boolean) {
        if (bool) {
            setupListeners()
        } else {
            silenceListeners()
        }
    }

    fun clickButton(ch : Char) {
        charToButton(ch).performClick()
    }

    fun clickEnter() {
        getViewById(R.id.buttonEnter).performClick()
    }

    fun clickDone() {
        getViewById(R.id.ClueModeSetter).performClick()
    }

    fun setKeyboardClueMode() {
        greyOutButtons()
        getViewById(R.id.ClueModeSetter).visibility = VISIBLE
        getViewById(R.id.buttonEnter).visibility = INVISIBLE

    }

    fun setKeyboardInputMode() {
        // the issue is here!
        updateButtonColor()
        getViewById(R.id.ClueModeSetter).visibility = INVISIBLE
        getViewById(R.id.buttonEnter).visibility = VISIBLE
    }

    fun updateButtonState(clue: LetterView.Clue, ch: Char) {
        if (clue == LetterView.Clue.DoesNotExist && buttonState[ch]?.let { checkIfLessThan(it, State.DoesNotExist) } == true) {
            buttonState[ch] = State.DoesNotExist
        } else if (clue == LetterView.Clue.Correct) {
            buttonState[ch] = State.Correct
        } else if (clue == LetterView.Clue.WrongPosition && buttonState[ch]?.let { checkIfLessThan(it, State.WrongPosition) } == true) {
            buttonState[ch] = State.WrongPosition
        }
        // buttonLetter[ch]?.setBackgroundColor(getColorForState(buttonState[ch]))
    }


    fun greyOutButtons() {
        for (letter in 'A'..'Z') {
            buttonLetter[letter]?.setBackgroundColor(resources.getColor(R.color.DisabledButton))
        }
        getViewById(R.id.buttonDelete).setBackgroundColor(resources.getColor(R.color.DisabledButton))
    }

    fun enableButton(ch: Char?, state: State) {
        buttonLetter[ch]?.setBackgroundColor(getColorForState(state))
        buttonLetter[ch]?.setOnClickListener {
            if (ch != null) {
                sendUpdateEventLetter(ch)
            }
        }
    }

    fun disableButton(ch: Char?) {
        buttonLetter[ch]?.setBackgroundColor(getColorForState(State.DoesNotExist))
        buttonLetter[ch]?.setOnClickListener(null)
    }

    // made this unprivate
    fun setupListeners() {
        Log.d("KeyboardView", "Setting up listeners")

        for (letter in 'A'..'Z') {
            buttonLetter[letter]?.setBackgroundColor(getColorForState(buttonState[letter]))
            buttonLetter[letter]?.setOnClickListener { sendUpdateEventLetter(letter) }
        }

        getViewById(R.id.buttonDelete).setOnClickListener { sendUpdateEventControl(ControlObserver.Control.Delete) }
        getViewById(R.id.buttonEnter).setOnClickListener { sendUpdateEventControl(ControlObserver.Control.Enter) }
        getViewById(R.id.ClueModeSetter).setOnClickListener { sendUpdateEventControl(ControlObserver.Control.ClueFinished) }
    }

    // made this unprivate
    fun silenceListeners() {
        for (button in touchables) {
            setOnClickListener(null)
        }
    }

    private fun updateButtonColor() {
        for (letter in 'A'..'Z') {
            buttonLetter[letter]?.setBackgroundColor(getColorForState(buttonState[letter]))
        }
        getViewById(R.id.buttonDelete).setBackgroundColor(resources.getColor(R.color.purple_500))
    }

    private fun getColorForState(state: State?) : Int {
        return when (state) {
            State.WrongPosition -> resources.getColor(R.color.Misplaced)
            State.DoesNotExist -> resources.getColor(R.color.DoesNotExist)
            State.Correct -> resources.getColor(R.color.Correct)
            else -> resources.getColor(R.color.purple_500)
        }
    }



    private fun charToButton(ch : Char): View {
        return buttonLetter[ch] as View
    }

    private fun initializeButtonMaps() {
        for (button in touchables) {
            val castButton = button as Button
            if (castButton.text.length == 1) {
                buttonLetter[castButton.text[0]] = castButton
                buttonState[castButton.text[0]] = State.Unknown
            }
        }
    }

    private fun checkIfLessThan(state1 : State, state2 : State) : Boolean {
        if (state1 == State.Unknown) {
            return true
        } else if (state1 == State.DoesNotExist) {
            return state2 != State.Unknown
        } else if (state1 == State.Correct) {
            return false
        } else {
            return state2 == State.Correct
        }
    }
}