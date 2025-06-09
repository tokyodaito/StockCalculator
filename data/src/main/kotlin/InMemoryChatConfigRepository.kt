package data

import java.util.concurrent.ConcurrentHashMap

class InMemoryChatConfigRepository(private val defaultConfig: ChatConfig = ChatConfig()) : ChatConfigRepository {
    private val configs = ConcurrentHashMap<Long, ChatConfig>()

    override fun getConfig(chatId: Long): ChatConfig =
        configs.computeIfAbsent(chatId) { defaultConfig.copy() }

    override fun update(chatId: Long, config: ChatConfig) {
        configs[chatId] = config
    }

    override fun allChatIds(): List<Long> = configs.keys.toList()
}
