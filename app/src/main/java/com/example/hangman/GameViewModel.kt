package com.example.hangman

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class Status { PLAYING, WON, LOST }

data class GameState(
    val word: String = "",
    val theme: String = "",
    val guessed: String = "",
    val errors: Int = 0,
    val score: Int = 0,
    val status: Status = Status.PLAYING,
    val recordSaved: Boolean = false
)

/**
 * Состояние игры хранится в ViewModel (переживает поворот экрана)
 * и дублируется в SavedStateHandle (переживает уничтожение процесса).
 */
class GameViewModel(app: Application, private val handle: SavedStateHandle) : AndroidViewModel(app) {

    companion object {
        const val MAX_ERRORS = 7
    }

    private val dao = AppDatabase.get(app).wordDao()
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()
    private var loading = false

    init {
        val word = handle.get<String>("word")
        if (word != null) {
            _state.value = GameState(
                word = word,
                theme = handle.get<String>("theme") ?: "",
                guessed = handle.get<String>("guessed") ?: "",
                errors = handle.get<Int>("errors") ?: 0,
                score = handle.get<Int>("score") ?: 0,
                status = Status.valueOf(handle.get<String>("status") ?: Status.PLAYING.name),
                recordSaved = handle.get<Boolean>("rec") ?: false
            )
        }
    }

    private fun commit(s: GameState) {
        _state.value = s
        handle["word"] = s.word
        handle["theme"] = s.theme
        handle["guessed"] = s.guessed
        handle["errors"] = s.errors
        handle["score"] = s.score
        handle["status"] = s.status.name
        handle["rec"] = s.recordSaved
    }

    /** Загадывает слово, если игра ещё не начата. */
    fun start(theme: String) {
        if (_state.value.word.isNotEmpty() || loading) return
        loading = true
        viewModelScope.launch {
            if (dao.count() == 0) dao.insertAll(Dictionary.all())
            val word = dao.wordsByTheme(theme).randomOrNull() ?: "ВИСЕЛИЦА"
            commit(GameState(word = word, theme = theme))
            loading = false
        }
    }

    fun restart(theme: String) {
        _state.value = GameState()
        start(theme)
    }

    fun guess(letter: Char) {
        val s = _state.value
        if (s.word.isEmpty() || s.status != Status.PLAYING || letter in s.guessed) return

        val guessed = s.guessed + letter
        val hits = s.word.count { it == letter }
        var errors = s.errors
        var score = s.score
        if (hits > 0) {
            score += hits * 10
        } else {
            errors++
            score = maxOf(0, score - 5)
        }

        var status = Status.PLAYING
        if (s.word.all { it in guessed }) {
            status = Status.WON
            score += 50 + (MAX_ERRORS - errors) * 10
        } else if (errors >= MAX_ERRORS) {
            status = Status.LOST
        }

        var saved = false
        if (status != Status.PLAYING && score > 0) {
            val date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
            saved = RecordsStorage.add(
                getApplication(),
                Record(score, s.theme, errors, s.word, date)
            )
        }
        commit(s.copy(guessed = guessed, errors = errors, score = score, status = status, recordSaved = saved))
    }
}
