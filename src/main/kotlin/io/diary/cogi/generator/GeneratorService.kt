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
    private val apiKey: String =
        dotenv["API_KEY"] ?: System.getenv("API_KEY") ?: throw IllegalStateException("API_KEY is missing!")

    private val client: WebClient = webClient.baseUrl("https://generativelanguage.googleapis.com/v1beta").build()

    private val prompt: String = """
        당신은 커밋 기록을 분석하여 작업 요약을 작성하는 AI입니다.  
        주어진 커밋 데이터를 기반으로, **작업 요약, 수정된 파일 목록, 일기**를 Markdown 형식으로 작성하세요.  
        **각 커밋의 Prefix에 따라 이모지를 추가**하여 직관적으로 표현하세요.  
        
        ---
        
        **✅ 이모지 규칙:**  
        - Feat: ✨ (새로운 기능)  
        - Fix: 🐛 (버그 수정)  
        - Chore: 🔧 (유지보수 작업)  
        - Refactor: ♻️ (리팩토링)  
        - Perf: ⚡️ (성능 개선)  
        - Test: ✅ (테스트 코드 작성)  
        - Docs: 📚 (문서화)  
        - Style: 💄 (코드 스타일 변경)   
        
        ---
        
        **✅ 출력 형식 (Markdown)**  
        ```
        # 1. 작업 요약  
        - 총 커밋 개수: {총 개수}개  
        - 작업 유형별 개수:  
          {존재하는 커밋 유형만 출력, 개수가 0인 유형은 제외}  
        
        # 2. 수정된 파일 목록  
        - 파일 총 개수: {파일 개수}개  
        - {파일명1}, {파일명2}, {파일명3}...  
        
        # 3. 일기  
        오늘은 {작업 요약}에 집중했습니다.  
        {어떤 기능을 개발하거나 수정했는지 자연스럽게 설명}.  
        {개발하면서 고려했던 점}.  
        {이후의 계획}.  
        ```
        
        ---
        
        **✅ 예제 입력 (커밋 데이터)**  
        ```
        Feat: OAuth 로그인 기능 추가. 수정된 파일: AuthController.kt, OAuthService.kt, SecurityConfig.kt  
        Feat: 게시글 검색 API 구현. 수정된 파일: PostController.kt, PostService.kt, PostRepository.kt  
        Feat: Kafka 이벤트 메시지 발행 기능 추가. 수정된 파일: EventPublisher.kt, KafkaConfig.kt  
        Refactor: 코드 리팩토링 및 패키지 구조 정리. 수정된 파일: PostService.kt, AuthService.kt  
        Chore: 로그 레벨 조정 및 디버그 로깅 추가. 수정된 파일: application.yml, LoggerConfig.kt  
        ```
        
        **✅ 예제 출력 (Markdown)**  
        ```
        # 1. 작업 요약  
        - 총 커밋 개수: 5개  
        - 작업 유형별 개수:  
          - Feat✨: 3개  
          - Refactor♻️: 1개  
          - Chore🔧: 1개  
        
        # 2. 수정된 파일 목록  
        - 파일 총 개수: 7개  
        - AuthController.kt, OAuthService.kt, SecurityConfig.kt, PostController.kt, PostService.kt, PostRepository.kt, LoggerConfig.kt  
        
        # 3. 일기  
        오늘은 OAuth 로그인 기능과 게시글 검색 API 개발을 진행했습니다.  
        Kafka를 활용한 이벤트 메시지 발행 기능도 추가했고, 
        기존 코드 구조를 개선하며 리팩토링 작업도 병행했습니다.  
        로그 레벨을 조정하고 디버깅을 강화하는 작업도 진행했습니다.  
        이제 API 테스트를 진행하고 최적화 작업을 할 예정입니다!  
        ```
        
        ---
        
        **📌 유의사항:**  
        - **커밋 개수를 자동 집계**하여 "총 커밋 개수"를 포함하세요.  
        - **모든 커밋 타입**(Feat, Fix, Refactor 등)을 자동 감지하여 개수를 계산하지만, **개수가 0인 경우 출력하지 마세요.**  
        - **수정된 파일 목록을 중복 없이 정리하고 개수를 표시**하세요.  
        - **일기는 5줄 내외로 자연스럽게 서술**하고 줄바꿈을 하세요.  
    """.trimIndent()


    fun processPrompt(request: GeneratorRequestDto): GeneratorResponseDto {
        // 기존 요청의 text 값 (커밋 메세지들) 가져오기
        val commitMessages = request.contents[0].parts.text

        // 프롬프트 추가
        val modifiedText = "다음은 오늘 내가 한 작업의 커밋 메시지 목록이다. \n" + commitMessages + "\n" + prompt

        // 기존 요청을 변경하지 않고 새로운 DTO 생성 (불변성 유지)
        val updatedRequest = request.copy(
            contents = listOf(
                GeneratorRequestDto.Content(
                    parts = GeneratorRequestDto.Parts(modifiedText)
                )
            )
        )

        return client.post().uri("/models/gemini-1.5-flash:generateContent?key=$apiKey")
            .contentType(MediaType.APPLICATION_JSON).bodyValue(updatedRequest).retrieve()
            .bodyToMono(GeneratorResponseDto::class.java).defaultIfEmpty(GeneratorResponseDto(emptyList(), null))
            .block()!!
    }
}