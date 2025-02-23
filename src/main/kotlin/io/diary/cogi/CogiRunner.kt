package io.diary.cogi

import org.apache.commons.logging.LogFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class CogiRunner(
) : ApplicationRunner {

    private val log = LogFactory.getLog(CogiRunner::class.java)

    override fun run(args: ApplicationArguments?) {
        log.info("Hello, Cogi!")
    }
}