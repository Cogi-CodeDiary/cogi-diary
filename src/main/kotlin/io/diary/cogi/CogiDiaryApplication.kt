package io.diary.cogi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CogiDiaryApplication

fun main(args: Array<String>) {
	runApplication<CogiDiaryApplication>(*args)
}
