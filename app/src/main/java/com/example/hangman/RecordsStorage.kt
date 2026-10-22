package com.example.hangman

import android.content.Context

data class Record(
    val score: Int,
    val theme: String,
    val errors: Int,
    val word: String,
    val date: String
)

/** Таблица рекордов в SharedPreferences (топ-10). */
object RecordsStorage {
    private const val PREFS = "records_prefs"
    private const val KEY = "records"
    private const val MAX = 10

    fun load(context: Context): List<Record> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "") ?: ""
        return raw.lines().filter { it.isNotBlank() }.mapNotNull { line ->
            val p = line.split("|")
            val score = p.getOrNull(0)?.toIntOrNull()
            if (p.size < 5 || score == null) null
            else Record(score, p[1], p[2].toIntOrNull() ?: 0, p[3], p[4])
        }
    }

    /** Возвращает true, если результат попал в таблицу. */
    fun add(context: Context, record: Record): Boolean {
        val list = (load(context) + record).sortedByDescending { it.score }.take(MAX)
        val raw = list.joinToString("\n") {
            "${it.score}|${it.theme}|${it.errors}|${it.word}|${it.date}"
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, raw).apply()
        return record in list
    }
}
