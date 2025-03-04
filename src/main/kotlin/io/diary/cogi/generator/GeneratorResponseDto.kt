package io.diary.cogi.generator

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true) // 불필요한 필드 무시
data class GeneratorResponseDto(
    val candidates: List<Candidate>? = emptyList(),
    val promptFeedback: PromptFeedback? = null
) {
    data class Candidate(
        val content: Content,
        val finishReason: String,
        val index: Int,
        @JsonProperty("safetyRatings") val safetyRatings: List<SafetyRating>? = emptyList() // null 허용 & 기본값 설정
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
        val safetyRatings: List<SafetyRating>? = emptyList() // nullable 처리
    )
}