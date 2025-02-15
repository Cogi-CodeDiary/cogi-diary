package io.diary.cogi.githubapi

import githubtest.dto.repository.Repository
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
class GitHubApiService(
    private val restTemplate: RestTemplate,
    private val gitHubProperties: GitHubProperties,
) {
    fun getAllRepositories(owner: String, page: Int = 1, perPage: Int = 100): List<Repository> {
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

}