package org.krithika.Kwordle

import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import org.krithika.Kwordle.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity() : AppCompatActivity(), ViewTreeObserver.OnGlobalLayoutListener {
    val COMPUTER = "COMPUTER"
    val HUMAN = "HUMAN"
    private lateinit var _binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        var player1 : Player
        var player2 : Player

        super.onCreate(savedInstanceState)

        val words = openDictionary()

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        val guesser = intent.getStringExtra("guesser")
        val generator = intent.getStringExtra("generator")
        val strictMode = intent.getBooleanExtra("strictMode", false)

        player1 = if (guesser == HUMAN) {
            playerFactory(Player.Type.HumanPlayer, words)
        } else {
            playerFactory(Player.Type.ComputerPlayer, words)
        }

        player2 = if (generator == HUMAN) {
            playerFactory(Player.Type.HumanPlayer, words)
        } else {
            playerFactory(Player.Type.ComputerPlayer, words)
        }

        /* val mode = intent.getStringExtra("mode")
        if (mode == "HvC") {
            player1 = playerFactory(Player.Type.HumanPlayer, words)
            player2 = playerFactory(Player.Type.ComputerPlayer, words)
        } else if (mode == "CvH") {
            player1 = playerFactory(Player.Type.ComputerPlayer, words)
            player2 = playerFactory(Player.Type.HumanPlayer, words)
        } else {
            player1 = playerFactory(Player.Type.HumanPlayer, words)
            player2 = playerFactory(Player.Type.HumanPlayer, words)
        } */
        _binding.GameView.setPlayer1(player1)
        _binding.GameView.setPlayer2(player2)
        _binding.GameView.setWordList(HashSet(words))
        _binding.GameView.setStrictMode(strictMode)
        _binding.keyboard.acceptInput(true)

        _binding.root.viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    private fun openDictionary(): HashSet<String> {
        val reader = BufferedReader(InputStreamReader(assets.open("dictionary.txt")))
        val words : HashSet<String> = HashSet()
        while (true) {
            val word = reader.readLine()
            if (word == null) {
                break
            } else {
                words.add(word)
            }
        }
        reader.close()
        return words
    }

    private fun playerFactory(type: Player.Type, words: HashSet<String>) : Player{
        if (type == Player.Type.HumanPlayer) {
            return HumanPlayer()
        }
        return ComputerPlayer(HashSet(words))
    }

    override fun onGlobalLayout() {
        _binding.GameView.startGame()
        _binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
    }
}
