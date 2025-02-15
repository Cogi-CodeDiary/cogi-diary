package io.diary.cogi.githubapi

import githubtest.dto.commit.CommitItem
import githubtest.dto.repository.Repository
import io.diary.cogi.utils.DateTimeUtils.toIsoString
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDateTime

@Service
class GitHubApiService(
    private val restTemplate: RestTemplate,
    private val gitHubProperties: GitHubProperties,
) {
    fun getAllRepositories(owner: String): List<Repository> {
        return generateSequence(1) { it + 1 }
            .map { page -> getRepositories(owner, page) }
            .takeWhile { it.isNotEmpty() }
            .flatten()
            .toList()
    }

    fun getRepositories(owner: String, page: Int = 1, perPage: Int = 100): List<Repository> {
        val url = UriComponentsBuilder.fromUriString(gitHubProperties.baseUrl)
            .path("/users/{owner}/repos")
            .queryParam("page", page)
            .queryParam("per_page", perPage)
            .buildAndExpand(owner)
            .toUri()

        val headers = HttpHeaders()
        headers.set("Authorization", "token ${gitHubProperties.token}")

        val entity = HttpEntity<String>(headers)

        val responseType = object : ParameterizedTypeReference<List<Repository>>() {}
        return restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            responseType
        ).body ?: emptyList()
    }

    fun getCommits(
        owner: String,
        repo: String,
        sinceDate: LocalDateTime = LocalDateTime.now().minusDays(7),
    ): List<CommitItem> {
        val parsedSinceDate = sinceDate.toIsoString()

        val url = UriComponentsBuilder.fromUriString(gitHubProperties.baseUrl)
            .path("/repos/{owner}/{repo}/commits")
            .queryParam("since", parsedSinceDate)
            .buildAndExpand(owner, repo)
            .toUri()

        val headers = HttpHeaders()
        headers.set("Authorization", "token ${gitHubProperties.token}")

        val entity = HttpEntity<String>(headers)

        val responseType = object : ParameterizedTypeReference<List<CommitItem>>() {}
        return restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            responseType
        ).body ?: emptyList()
    }

}