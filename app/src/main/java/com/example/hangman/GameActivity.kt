package com.example.hangman

import android.content.res.Configuration
import android.os.Bundle
import android.widget.GridLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hangman.databinding.ActivityGameBinding
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

/** Экран 2: игровой. */
class GameActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_THEME = "theme"
    }

    private lateinit var binding: ActivityGameBinding
    private val viewModel: GameViewModel by viewModels()
    private val letterButtons = mutableMapOf<Char, MaterialButton>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val theme = intent.getStringExtra(EXTRA_THEME) ?: Dictionary.themes.first()
        binding.tvTheme.text = "Тема: $theme"

        buildKeyboard()
        viewModel.start(theme)

        binding.btnAgain.setOnClickListener { viewModel.restart(theme) }
        binding.btnMenu.setOnClickListener { finish() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it) }
            }
        }
    }

    private fun buildKeyboard() {
        val landscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        binding.keyboard.columnCount = if (landscape) 11 else 8
        for (letter in Dictionary.ALPHABET) {
            val button = MaterialButton(this).apply {
                text = letter.toString()
                textSize = 16f
                minWidth = 0
                minimumWidth = 0
                insetTop = 0
                insetBottom = 0
                setPadding(0, 0, 0, 0)
                setOnClickListener { viewModel.guess(letter) }
            }
            val params = GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED, 1f),
                GridLayout.spec(GridLayout.UNDEFINED, 1f)
            ).apply {
                width = 0
                setMargins(3, 3, 3, 3)
            }
            binding.keyboard.addView(button, params)
            letterButtons[letter] = button
        }
    }

    private fun render(s: GameState) {
        if (s.word.isEmpty()) {
            binding.tvWord.text = "…"
            return
        }
        binding.tvWord.text = s.word.map { if (it in s.guessed) it else '_' }.joinToString(" ")
        binding.hangman.errors = s.errors
        binding.tvErrors.text = "Ошибки: ${s.errors} / ${GameViewModel.MAX_ERRORS}"
        binding.tvScore.text = "Очки: ${s.score}"

        letterButtons.forEach { (letter, button) ->
            button.isEnabled = s.status == Status.PLAYING && letter !in s.guessed
        }

        binding.resultPanel.isVisible = s.status != Status.PLAYING
        if (s.status != Status.PLAYING) {
            binding.tvResultTitle.text = if (s.status == Status.WON) "Победа!" else "Поражение"
            binding.tvResultInfo.text = buildString {
                append("Загаданное слово: ${s.word}\n")
                append("Ошибок: ${s.errors}\n")
                append("Очки: ${s.score}")
                if (s.recordSaved) append("\nРезультат записан в таблицу рекордов")
            }
        }
    }
}
