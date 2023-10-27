package com.tonnoz.errorhandling

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ErrorhandlingApplication

fun main(args: Array<String>) {
	runApplication<ErrorhandlingApplication>(*args)
}
