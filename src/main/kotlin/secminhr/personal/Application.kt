package secminhr.personal

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.doublereceive.*
import kotlinx.serialization.json.Json
import secminhr.personal.plugins.configureRouting



fun main() {
    embeddedServer(Netty, port = 80, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureRouting()
    install(ContentNegotiation) {
        json(Json {
            this.ignoreUnknownKeys = true
        })
    }
    install(DoubleReceive)
}
