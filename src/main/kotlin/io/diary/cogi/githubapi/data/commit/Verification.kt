package githubtest.dto.commit

data class Verification(
    val payload: Any?,
    val reason: String,
    val signature: Any?,
    val verified: Boolean,
    val verified_at: Any?,
)