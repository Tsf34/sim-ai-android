package com.fabrice.simai.data

import com.fabrice.simai.data.db.ConversationEntity
import com.fabrice.simai.data.db.ConversationDao
import com.fabrice.simai.data.db.MessageDao
import com.fabrice.simai.data.db.SimAiDatabase
import com.fabrice.simai.data.model.Conversation
import com.fabrice.simai.data.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Repository des conversations : historique local (Room). */
class ConversationRepository(database: SimAiDatabase) {

    private val conversationDao: ConversationDao = database.conversationDao()
    private val messageDao: MessageDao = database.messageDao()

    /** Historique des conversations, plus récente d'abord. */
    val conversations: Flow<List<Conversation>> =
        conversationDao.conversations().map { entities -> entities.map(::toConversation) }

    fun createConversation(opencodeSessionId: String, title: String): Conversation {
        val now = System.currentTimeMillis()
        val entity = ConversationEntity(
            opencodeSessionId = opencodeSessionId,
            title = title,
            createdAt = now,
            updatedAt = now
        )
        conversationDao.insert(entity)
        return toConversation(entity)
    }

    fun messagesOf(conversationId: Long): Flow<List<Message>> =
        messageDao.messages(conversationId).map { entities ->
            entities.map { Message(it.role, it.content, it.timestamp) }
        }

    fun addMessage(conversationId: Long, message: Message) {
        messageDao.insert(
            com.fabrice.simai.data.db.MessageEntity(
                conversationId = conversationId,
                role = message.role,
                content = message.content,
                timestamp = message.timestamp
            )
        )
    }

    fun updateTitle(conversation: Conversation, title: String) {
        conversationDao.updateTitle(conversation.id, title, System.currentTimeMillis())
    }

    fun touch(conversation: Conversation) {
        conversationDao.touch(conversation.id, System.currentTimeMillis())
    }

    fun deleteConversation(conversation: Conversation) {
        conversationDao.deleteById(conversation.id)
    }

    private fun toConversation(entity: ConversationEntity): Conversation =
        Conversation(
            id = entity.id,
            opencodeSessionId = entity.opencodeSessionId,
            title = entity.title,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
}
