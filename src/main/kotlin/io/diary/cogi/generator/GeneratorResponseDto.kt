package io.diary.cogi.generator

data class GeneratorResponseDto(
    val candidates: List<Candidate> = emptyList(),
    val promptFeedback: PromptFeedback? = null
) {
    data class Candidate(
        val content: Content,
        val finishReason: String,
        val index: Int,
        val safetyRatings: List<SafetyRating>
    )

    data class Content(
        val parts: List<Parts>,
        val role: String
    )

    data class Parts(
        val text: String
    )

    data class SafetyRating(
        val category: String,
        val probability: String
    )

    data class PromptFeedback(
        val safetyRatings: List<SafetyRating>
    )
}