package com.example.hangman

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hangman.databinding.ActivityMainBinding

/** Экран 1: меню (выбор темы, старт, таблица рекордов). */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.hangmanPreview.errors = 4

        binding.spinnerTheme.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, Dictionary.themes
        )

        binding.btnStart.setOnClickListener {
            val theme = binding.spinnerTheme.selectedItem as String
            startActivity(Intent(this, GameActivity::class.java).putExtra(GameActivity.EXTRA_THEME, theme))
        }
    }

    override fun onResume() {
        super.onResume()
        fillRecords()
    }

    private fun fillRecords() {
        val table = binding.tableRecords
        table.removeAllViews()
        val records = RecordsStorage.load(this)
        if (records.isEmpty()) {
            table.addView(row("Рекордов пока нет", bold = false, span = true))
            return
        }
        table.addView(row("#", "Очки", "Тема", "Слово", "Дата", bold = true))
        records.forEachIndexed { i, r ->
            table.addView(row("${i + 1}", "${r.score}", r.theme, r.word, r.date))
        }
    }

    private fun row(vararg cells: String, bold: Boolean = false, span: Boolean = false): TableRow {
        val row = TableRow(this)
        cells.forEach { text ->
            val tv = TextView(this).apply {
                this.text = text
                setPadding(8, 8, 8, 8)
                textSize = 14f
                gravity = if (span) Gravity.CENTER else Gravity.START
                if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
            }
            row.addView(tv)
        }
        return row
    }
}
