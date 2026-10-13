package com.oenexa.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerResponse.ok

@SpringBootApplication
class OenexaAiEngineerApplication {
    @Bean
    fun router(): RouterFunction<org.springframework.web.reactive.function.server.ServerResponse> =
        route(GET("/health")) { ok().bodyValue("OK") }
}

fun main(args: Array<String>) {
    runApplication<OenexaAiEngineerApplication>(*args)
}
