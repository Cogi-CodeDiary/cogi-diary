package io.diary.cogi.githubapi

import githubtest.dto.commit.CommitItem
import githubtest.dto.repository.Repository
import org.apache.commons.logging.LogFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@Component
class GitHubApiRunner(
    private val githubProperties: GitHubProperties,
    private val gitHubApiService: GitHubApiService,
) : ApplicationRunner {

    private val log = LogFactory.getLog(GitHubApiRunner::class.java)

    override fun run(args: ApplicationArguments?) {
        runBlocking {
            val repositories = mutableListOf<Repository>()

            // Collect all repositories from the Flow
            gitHubApiService
                .getAllRepositories(githubProperties.username)
                .collect { repoPage ->
                    repositories.addAll(repoPage)
                }

            // Process repositories in parallel with coroutines
            val repositoriesWithCommits = repositories
                .map { repository ->
                    async {
                        val commits = gitHubApiService.getCommits(
                            owner = githubProperties.username,
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
    }
}