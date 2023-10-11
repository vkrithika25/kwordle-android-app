package org.krithika.Kwordle

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import org.krithika.Kwordle.databinding.StartingScreenActivityBinding

class StartingScreenActivity : AppCompatActivity() {
    val COMPUTER = "COMPUTER"
    val HUMAN = "HUMAN"
    private lateinit var _binding : StartingScreenActivityBinding
    private lateinit var _guesser : String
    private lateinit var _generator : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = StartingScreenActivityBinding.inflate(layoutInflater)
        setContentView(_binding.root)
        setupDoneButtonListener()
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.HumanGuesser ->
                    if (checked) {
                        _guesser = HUMAN
                    }
                R.id.ComputerGuesser ->
                    if (checked) {
                        _guesser = COMPUTER
                    }
                R.id.HumanGenerator ->
                    if (checked) {
                        _generator = HUMAN
                    }
                R.id.ComputerGenerator ->
                    if (checked) {
                        _generator = COMPUTER
                    }
            }
        }
    }

    private fun setupDoneButtonListener() {
        _binding.Done.setOnClickListener {
            if (radioButtonsAreSet()) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("guesser", _guesser)
                intent.putExtra("generator", _generator)
                intent.putExtra("strictMode", isStrictModeSet())
                startActivity(intent)
            } else {
                val text = "Please select your guesser and generator!"
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(this, text, duration)
                toast.show()
            }
        }
    }

    private fun radioButtonsAreSet() : Boolean {
        return !(_binding.Guesser.checkedRadioButtonId == -1 || _binding.Generator.checkedRadioButtonId == -1)
    }

    private fun isStrictModeSet() : Boolean {
        val toggle: SwitchCompat = findViewById(R.id.StrictMode)
        return toggle.isChecked
    }
}