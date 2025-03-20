package io.diary.cogi

import githubtest.dto.commit.CommitItem
import githubtest.dto.repository.Repository
import io.diary.cogi.githubapi.GitHubApiService
import io.diary.cogi.githubapi.GitHubProperties
import io.diary.cogi.generator.GeneratorRequestDto
import io.diary.cogi.generator.GeneratorService
import org.apache.commons.logging.LogFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import kotlinx.coroutines.*

@Component
class CogiRunner(
    private val gitHubProperties: GitHubProperties,
    private val gitHubApiService: GitHubApiService,
    private val generatorService: GeneratorService
) : ApplicationRunner {

    private val log = LogFactory.getLog(CogiRunner::class.java)

    override fun run(args: ApplicationArguments?) {
        val allRepositoriesWithCommits = gitHubApiService.getAllRepositoriesWithCommits()

        allRepositoriesWithCommits.forEach { (repository, commits) ->
            log.info("Repository: ${repository.name}")

            commits.forEach { commit ->
                log.info("  - ${commit.commit.message} ${commit.commit.author.date}")
            }
        }
    }

    fun addMarkdownFile(owner: String, repo: String, filePath: String, content: String, branch: String = "main") {
        gitHubApiService.addMarkdownFile(
            owner = gitHubProperties.username,
            repo = gitHubProperties.targetRepository,
            filePath = "diary.md",
            content = "Hello, GitHub API!",
        )
        return

        runBlocking {
            val repositories = mutableListOf<Repository>()

            gitHubApiService
                .getAllRepositories(gitHubProperties.username)
                .collect { repoPage ->
                    repositories.addAll(repoPage)
                }

            val repositoriesWithCommits = repositories
                .map { repository ->
                    async {
                        val commits = gitHubApiService.getCommits(
                            owner = gitHubProperties.username,
                            repo = repository.name
                        )
                        repository to commits
                    }
                }
                .awaitAll()
                .toMap()

            loggingRepositoriesWithCommits(repositoriesWithCommits)
        }
    }

    private fun loggingRepositoriesWithCommits(repositoriesWithCommits: Map<Repository, List<CommitItem>>) {
        repositoriesWithCommits.forEach { (repository, commits) ->
            log.info("Repository: ${repository.name}")

            commits.forEach { commit ->
                log.info("  - ${commit.commit.message} ${commit.commit.author.date}")
            }
        }
        log.info("Hello, Cogi!")
    }

    fun gemini() {
        // 예제 커밋 메시지 리스트
        val commitMessages = listOf(
            "Fix: GeneratorResponseDto에서 safetyRatings 필드 null 처리 및 기본값 설정. 수정된 파일: GeneratorResponseDto.java",
            "Feat: 예제 커밋 메시지 리스트로 제미나이 request 생성 후 generatorService 사용해서 호출 로직. 수정된 파일: CogiRunner.java",
            "Fix: dotenv로 설정. 수정된 파일: GeneratorService.java",
            "Chore: .env 파일 gitignore에 추가. 수정된 파일: .gitignore",
            "Feat: GeneratorService 추가. 수정된 파일: GeneratorService.java",
            "Feat: GeneratorResponseDto 추가. 수정된 파일: GeneratorResponseDto.java",
            "Feat: GeneratorRequestDto 추가. 수정된 파일: GeneratorRequestDto.java",
            "Feat: WebClient 설정 추가. 수정된 파일: WebClientConfig.java"
        )


        // 제미나이 request 생성
        val requestDto = GeneratorRequestDto(
            contents = listOf(
                GeneratorRequestDto.Content(
                    parts = GeneratorRequestDto.Parts(commitMessages.joinToString("\n"))
                )
            ),
            generationConfig = GeneratorRequestDto.GenerationConfig(1, 1000, 0.7)
        )

        // GeneratorService 호출
        val response = generatorService.processPrompt(requestDto)

        // 결과
        log.info("Generated Diary: ${response.candidates}")
    }
}