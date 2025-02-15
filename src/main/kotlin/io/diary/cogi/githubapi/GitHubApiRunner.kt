package io.diary.cogi.githubapi

import org.apache.commons.logging.LogFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

import kotlinx.coroutines.*

@Component
class GitHubApiRunner(
    private val githubProperties: GitHubProperties,
    private val gitHubApiService: GitHubApiService,
) : ApplicationRunner {

    private val log = LogFactory.getLog(GitHubApiRunner::class.java)

    override fun run(args: ApplicationArguments?) {
        val repositories = gitHubApiService.getAllRepositories(githubProperties.username)

        runBlocking {
            val repositoriesWithCommits = repositories.map { repository ->
                async {
                    val commits = gitHubApiService.getCommits(githubProperties.username, repository.name)
                    repository to commits
                }
            }.awaitAll().toMap()

            repositoriesWithCommits.forEach { (repository, commits) ->
                log.info("Repository: ${repository.name}")
                commits.forEach { commit ->
                    log.info("  - ${commit.commit.message} ${commit.commit.author.date}")
                }
            }
        }
    }
}