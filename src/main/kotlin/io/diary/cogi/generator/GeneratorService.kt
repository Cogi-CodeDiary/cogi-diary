package io.diary.cogi.generator

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class GeneratorService(
    private val webClient: WebClient.Builder
) {
    private val dotenv = Dotenv.configure().ignoreIfMissing().load()
    private val apiKey: String = dotenv["API_KEY"] ?: System.getenv("API_KEY")
    ?: throw IllegalStateException("API_KEY is missing!")

    private val client: WebClient = webClient.baseUrl("https://generativelanguage.googleapis.com/v1beta").build()

    fun processPrompt(request: GeneratorRequestDto): GeneratorResponseDto {
        // 기존 요청의 text 값 가져오기
        val originalText = request.contents[0].parts.text

        // 프롬프트 추가
        val modifiedText = """
            다음은 오늘 내가 한 작업의 커밋 메시지 목록이다. 
            $originalText
            이 내용을 바탕으로 하루를 회고하는 일기 형식으로 변환해줘.
        """.trimIndent()

        // 기존 요청을 변경하지 않고 새로운 DTO 생성 (불변성 유지)
        val updatedRequest = request.copy(
            contents = listOf(
                GeneratorRequestDto.Content(
                    parts = GeneratorRequestDto.Parts(modifiedText)
                )
            )
        )

        return client.post()
            .uri("/models/gemini-1.5-flash:generateContent?key=$apiKey")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updatedRequest)
            .retrieve()
            .bodyToMono(GeneratorResponseDto::class.java)
            .defaultIfEmpty(GeneratorResponseDto(emptyList(), null))
            .block()!!
    }
}