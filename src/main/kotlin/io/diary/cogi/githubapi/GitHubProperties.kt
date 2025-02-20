package io.diary.cogi.githubapi

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "github")
class GitHubProperties(
    var username: String = "",
    var token: String = "",
    var baseUrl: String = "https://api.github.com",
    var targetRepository: String = "",
)
