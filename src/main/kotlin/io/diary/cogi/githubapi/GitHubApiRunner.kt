package io.diary.cogi.githubapi

import org.apache.commons.logging.LogFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class GitHubApiRunner(
    private val githubProperties: GitHubProperties,
    private val gitHubApiService: GitHubApiService,
) : ApplicationRunner {

    private val log = LogFactory.getLog(GitHubApiRunner::class.java)

    override fun run(args: ApplicationArguments?) {
        val repositories = gitHubApiService.getAllRepositories(githubProperties.username)

        repositories.forEach { repository ->
            val commits = gitHubApiService.getCommits(githubProperties.username, repository.name)
            log.info("Repository: ${repository.name}")
            if (commits.isEmpty()) {
                log.info("-- No commits found --")
            }
            commits.forEach { commit ->
                log.info("Commit: ${commit.commit.message}")
            }
        }
    }
}