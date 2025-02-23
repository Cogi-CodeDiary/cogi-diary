package io.diary.cogi

import githubtest.dto.commit.CommitItem
import githubtest.dto.repository.Repository
import io.diary.cogi.githubapi.GitHubApiService
import io.diary.cogi.githubapi.GitHubProperties
import org.apache.commons.logging.LogFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import kotlinx.coroutines.*

@Component
class CogiRunner(
    private val githubProperties: GitHubProperties,
    private val gitHubApiService: GitHubApiService,
) : ApplicationRunner {

    private val log = LogFactory.getLog(CogiRunner::class.java)

    override fun run(args: ApplicationArguments?) {

        //    fun addMarkdownFile(owner: String, repo: String, filePath: String, content: String, branch: String = "main") {

//        gitHubApiService.addMarkdownFile(
//            owner = githubProperties.username,
//            repo = githubProperties.targetRepository,
//            filePath = "diary.md",
//            content = "Hello, GitHub API!",
//        )
//        return
//
//        runBlocking {
//            val repositories = mutableListOf<Repository>()
//
//            gitHubApiService
//                .getAllRepositories(githubProperties.username)
//                .collect { repoPage ->
//                    repositories.addAll(repoPage)
//                }
//
//            val repositoriesWithCommits = repositories
//                .map { repository ->
//                    async {
//                        val commits = gitHubApiService.getCommits(
//                            owner = githubProperties.username,
//                            repo = repository.name
//                        )
//                        repository to commits
//                    }
//                }
//                .awaitAll()
//                .toMap()
//
//            loggingRepositoriesWithCommits(repositoriesWithCommits)
//        }
    }

    private fun loggingRepositoriesWithCommits(repositoriesWithCommits: Map<Repository, List<CommitItem>>) {
        repositoriesWithCommits.forEach { (repository, commits) ->
            log.info("Repository: ${repository.name}")

            commits.forEach { commit ->
                log.info("  - ${commit.commit.message} ${commit.commit.author.date}")
            }
        }
    }
}