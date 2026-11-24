package com.fabrice.simai.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Query("SELECT * FROM conversations ORDER BY updated_at DESC, id DESC")
    fun conversations(): Flow<List<ConversationEntity>>

    @Insert
    fun insert(conversation: ConversationEntity)

    @Query("UPDATE conversations SET title = :title, updated_at = :updatedAt WHERE id = :id")
    fun updateTitle(id: Long, title: String, updatedAt: Long)

    @Query("UPDATE conversations SET updated_at = :updatedAt WHERE id = :id")
    fun touch(id: Long, updatedAt: Long)

    @Query("DELETE FROM conversations WHERE id = :id")
    fun deleteById(id: Long)
}
