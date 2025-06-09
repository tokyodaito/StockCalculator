package data

interface ChatConfigRepository {
    fun getConfig(chatId: Long): ChatConfig
    fun update(chatId: Long, config: ChatConfig)
    fun allChatIds(): List<Long>
}
