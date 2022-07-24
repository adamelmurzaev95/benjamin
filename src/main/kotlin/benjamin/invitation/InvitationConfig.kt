package benjamin.invitation

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.CountDownLatch

@Configuration
class InvitationConfig {
    @Bean
    fun monitorCountDownLatch() = CountDownLatch(2)
}
