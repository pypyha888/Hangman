package com.example.hangman

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WordDao {
    @Query("SELECT word FROM words WHERE theme = :theme")
    suspend fun wordsByTheme(theme: String): List<String>

    @Query("SELECT COUNT(*) FROM words")
    suspend fun count(): Int

    @Insert
    suspend fun insertAll(words: List<WordEntity>)
}
