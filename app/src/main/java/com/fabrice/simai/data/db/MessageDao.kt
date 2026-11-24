package com.fabrice.simai.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY timestamp ASC, id ASC")
    fun messages(conversationId: Long): Flow<List<MessageEntity>>

    @Insert
    fun insert(message: MessageEntity)
}
