package webapp

import data.ChatConfig
import data.ChatConfigRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/config")
class ChatConfigController(private val repo: ChatConfigRepository) {
    @GetMapping("/{chatId}")
    fun getConfig(@PathVariable chatId: Long): ChatConfig = repo.getConfig(chatId)

    @PostMapping("/{chatId}")
    fun updateConfig(@PathVariable chatId: Long, @RequestBody cfg: ChatConfig): ChatConfig {
        repo.update(chatId, cfg)
        return repo.getConfig(chatId)
    }
}
