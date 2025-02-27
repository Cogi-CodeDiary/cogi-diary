package io.diary.cogi

import io.diary.cogi.generator.GeneratorRequestDto
import io.diary.cogi.generator.GeneratorService
import org.apache.commons.logging.LogFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class CogiRunner(
    private val generatorService: GeneratorService
) : ApplicationRunner {

    private val log = LogFactory.getLog(CogiRunner::class.java)

    override fun run(args: ApplicationArguments?) {
        log.info("Hello, Cogi!")

        // 예제 커밋 메시지 리스트
        val commitMessages = listOf(
            "Implemented user login functionality",
            "Fixed a bug where the password reset link was not working",
            "Refactored database connection handling",
            "Updated UI design for better user experience"
        )

        // 제미나이 request 생성
        val requestDto = GeneratorRequestDto(
            contents = listOf(
                GeneratorRequestDto.Content(
                    parts = GeneratorRequestDto.Parts(commitMessages.joinToString("\n"))
                )
            ),
            generationConfig = GeneratorRequestDto.GenerationConfig(1,1000,0.7)
        )

        // GeneratorService 호출
        val response = generatorService.processPrompt(requestDto)

        // 결과
        log.info("Generated Diary: ${response.candidates}")

    }
}