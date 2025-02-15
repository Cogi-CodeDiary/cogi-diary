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
    /**
     * 사용자의 모든 저장소 리스트를 조회하는 함수
     *
     * @param owner 저장소 소유자의 GitHub 사용자명
     * @return 해당 사용자의 모든 저장소 리스트
     */
    fun getAllRepositories(owner: String): List<Repository> {
        return generateSequence(1) { it + 1 }
            .map { page -> getRepositories(owner, page) }
            .takeWhile { it.isNotEmpty() }
            .flatten()
            .toList()
    }

    /**
     * 사용자의 저장소 리스트를 조회하는 함수
     * Github API로 한 번에 최대 100개의 저장소를 조회할 수 있음
     *
     * @param owner 저장소 소유자의 GitHub 사용자명
     * @param page 조회할 페이지 번호
     * @param perPage 한 페이지에 조회할 아이템 수 (기본값 100개, GitHub API 상 최대값 100개)
     * @return 해당 사용자의 저장소 리스트
     */
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

    /**
     * 특정 저장소의 커밋 목록을 조회하는 함수
     *
     * @param owner 저장소 소유자의 GitHub 사용자명
     * @param repo 조회할 저장소 이름
     * @param sinceDate 이 날짜 이후의 커밋을 조회 (기본값: 7일 전)
     * @param untilDate 이 날짜 이전의 커밋을 조회 (기본값: 현재 시각)
     * @return 해당 기간 내의 커밋 리스트
     */

    fun getCommits(
        owner: String,
        repo: String,
        sinceDate: LocalDateTime = LocalDateTime.now().minusDays(7),
        untilDate: LocalDateTime = LocalDateTime.now(),
    ): List<CommitItem> {
        val url = UriComponentsBuilder.fromUriString(gitHubProperties.baseUrl)
            .path("/repos/{owner}/{repo}/commits")
            .queryParam("since", sinceDate.toIsoString())
            .queryParam("until", untilDate.toIsoString())
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