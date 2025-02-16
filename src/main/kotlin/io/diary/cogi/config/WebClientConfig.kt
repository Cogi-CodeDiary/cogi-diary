package io.diary.cogi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    @Bean
    fun webClient(): WebClient {
        return WebClient.builder()
            .codecs { clientCodecConfigure ->
                // 버퍼 크기를 10MB로 증가
                clientCodecConfigure
                    .defaultCodecs()
                    .maxInMemorySize(10 * 1024 * 1024)
            }
            .build()
    }
}