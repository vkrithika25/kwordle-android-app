package org.krithika.Kwordle

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView

class LetterView(_context: Context, _attrs: AttributeSet, private val _keyboardId: Int) : AppCompatTextView(_context, _attrs), LetterObserver {
    enum class Clue {
        Unknown,
        Correct,
        WrongPosition,
        DoesNotExist
    }

    enum class LetterMode {
        InputMode,
        ClueMode,
        DoneMode
    }

    private val CLEAR_LETTER = ' '
    private var _clue: Clue = Clue.Unknown
    private var _letterMode: LetterMode = LetterMode.InputMode
    private var _isActiveInput = false
    private var _keyboard: Keyboard? = null

    init {
        updateBackgroundColor()

        width = 150
        textAlignment = TEXT_ALIGNMENT_CENTER
        setTextIsSelectable(false)
        setTextColor(resources.getColor(R.color.LetterViewText))
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 64.0f)
        isAllCaps = true
        isClickable = true

        clearLetter()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        _keyboard = rootView.findViewById(_keyboardId)
        setIsActiveInput(_isActiveInput) // Just to setup the observers
    }

    fun setClue(clue : Clue) {
        _clue = clue
        updateBackgroundColor()
    }

    fun setMode(mode : LetterMode) {
        _letterMode = mode
        when (mode) {
            LetterMode.InputMode -> {
                inputModeActions()
            }
            LetterMode.ClueMode -> {
                clueModeActions()
            }
            LetterMode.DoneMode -> {
                doneModeActions()
            }
        }
    }


    fun getClue() : Clue {
        return _clue
    }

    fun getLetter(): Char {
        return text[0]
    }

    fun setLetter(ch: Char) {
        if (ch == CLEAR_LETTER) {
            text = " "
        } else {
            text = ch.toString()
        }
        updateBackgroundColor()
    }

    fun setIsActiveInput(state : Boolean) {
        _isActiveInput = state
        if (_isActiveInput) {
            _keyboard?.addLetterObserver(this)
        } else {
            _keyboard?.removeLetterObserver(this)
        }
        updateBackgroundColor()
    }

    fun clearLetter() {
        setLetter(CLEAR_LETTER)
    }

    private fun updateBackgroundColor() {
        val gd = GradientDrawable()
        gd.setStroke(6, getOutlineColor())
        gd.setColor(getBackgroundColor())
        background = gd
    }

    private fun getBackgroundColor(): Int {
        return when (_clue) {
            Clue.Correct -> resources.getColor(R.color.Correct)
            Clue.DoesNotExist -> resources.getColor(R.color.DoesNotExist)
            Clue.WrongPosition -> resources.getColor(R.color.Misplaced)
            Clue.Unknown -> resources.getColor(R.color.Unknown)
        }
    }

    private fun getOutlineColor(): Int {
        return if (_isActiveInput) resources.getColor((R.color.Current))
            else resources.getColor(R.color.NotCurrent)
    }

    private fun inputModeActions() {
        setOnClickListener(null)
    }

    private fun clueModeActions() {
        setOnClickListener{
            changeClueState()
            updateBackgroundColor()
        }
    }

    private fun doneModeActions() {
        setOnClickListener(null)
    }

    private fun changeClueState() {
        when (_clue) {
            Clue.Unknown ->  {
                _clue = Clue.DoesNotExist
                _keyboard?.updateButtonState(Clue.DoesNotExist, getLetter())
            }
            Clue.DoesNotExist -> {
                _clue = Clue.WrongPosition
                _keyboard?.updateButtonState(Clue.WrongPosition, getLetter())
            }
            Clue.WrongPosition -> {
                _clue = Clue.Correct
                _keyboard?.updateButtonState(Clue.Correct, getLetter())
            }
            Clue.Correct -> {
                _clue = Clue.DoesNotExist
                _keyboard?.updateButtonState(Clue.DoesNotExist, getLetter())
            }

        }
    }

    override fun updateLetter(ch : Char) {
        text = ch.toString()
    }
}