package io.diary.cogi.generator

data class GeneratorRequestDto(
    val contents: List<Content>,
    val generationConfig: GenerationConfig
) {
    data class Content(
        val parts: Parts
    )

    data class Parts(
        val text: String
    )

    data class GenerationConfig(
        val candidate_count: Int = 1,
        val max_output_tokens: Int = 1000,
        val temperature: Double = 0.7
    )
}