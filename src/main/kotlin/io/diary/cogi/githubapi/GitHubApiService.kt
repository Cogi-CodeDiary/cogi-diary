package io.diary.cogi.githubapi

import githubtest.dto.commit.CommitItem
import githubtest.dto.repository.Repository
import io.diary.cogi.utils.DateTimeUtils.toIsoString
import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDateTime
import kotlinx.coroutines.reactor.awaitSingle
import org.apache.juli.logging.LogFactory
import java.util.*

@Service
class GitHubApiService(
    private val webClient: WebClient,
    private val gitHubProperties: GitHubProperties,
) {

    private val log = LogFactory.getLog(GitHubApiService::class.java)

    /**
     * 사용자의 모든 저장소 리스트를 조회하는 함수
     *
     * @param owner 저장소 소유자의 GitHub 사용자명
     * @return 해당 사용자의 모든 저장소 리스트를 포함하는 Flow
     */
    fun getAllRepositories(owner: String): Flow<List<Repository>> =
        generateSequence(1) { it + 1 }
            .asFlow()
            .map { page -> getRepositories(owner, page) }
            .takeWhile { repositories -> repositories.isNotEmpty() }

    /**
     * 사용자의 저장소 리스트를 조회하는 함수
     * Github API로 한 번에 최대 100개의 저장소를 조회할 수 있음
     *
     * @param owner 저장소 소유자의 GitHub 사용자명
     * @param page 조회할 페이지 번호
     * @param perPage 한 페이지에 조회할 아이템 수 (기본값 100개, GitHub API 상 최대값 100개)
     * @return 해당 사용자의 저장소 리스트
     */
    suspend fun getRepositories(owner: String, page: Int = 1, perPage: Int = 100): List<Repository> {
        val url = UriComponentsBuilder.fromUriString(gitHubProperties.baseUrl)
            .path("/users/{owner}/repos")
            .queryParam("page", page)
            .queryParam("per_page", perPage)
            .buildAndExpand(owner)
            .toUri()

        return webClient.get()
            .uri(url)
            .header("Authorization", "token ${gitHubProperties.token}")
            .retrieve()
            .bodyToMono<List<Repository>>()
            .awaitSingle()
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
    suspend fun getCommits(
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

        return webClient.get()
            .uri(url)
            .header("Authorization", "token ${gitHubProperties.token}")
            .retrieve()
            .bodyToMono<List<CommitItem>>()
            .awaitSingle()
    }

    fun addMarkdownFile(owner: String, repo: String, filePath: String, content: String, branch: String = "main") {
        val encodedContent = Base64.getEncoder().encodeToString(content.toByteArray())

        val requestBody = mapOf(
            "message" to "Add new markdown file",
            "content" to encodedContent,
            "branch" to branch
        )

        log.info("파일 업로드 요청: $owner/$repo/$filePath")

        val uri = UriComponentsBuilder.fromUriString(gitHubProperties.baseUrl)
            .path("/repos/{owner}/{repo}/contents/{path}")
            .buildAndExpand(owner, repo, filePath)

        webClient.put()
            .uri(uri.toUri())
            .header("Authorization", "token ${gitHubProperties.token}")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(String::class.java)
            .doOnSuccess { log.info("파일 업로드 성공: $it") }
            .doOnError { e -> log.info("파일 업로드 실패: ${e.message}") }
            .subscribe()
    }

}