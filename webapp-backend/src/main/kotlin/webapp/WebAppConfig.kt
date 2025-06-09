package webapp

import data.ChatConfigRepository
import data.InMemoryChatConfigRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class WebAppConfig {
    @Bean
    fun chatConfigRepository(): ChatConfigRepository = InMemoryChatConfigRepository()
}
