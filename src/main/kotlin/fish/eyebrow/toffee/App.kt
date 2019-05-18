package fish.eyebrow.toffee

import fish.eyebrow.toffee.route.Chat.chat
import io.ktor.application.Application
import io.ktor.routing.routing
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

object App {
    @JvmStatic
    fun main(args: Array<String>) {
        embeddedServer(Netty, commandLineEnvironment(args)).start(true)
    }

    fun Application.toffeeCommon() {
        routing {
            chat()
        }
    }
}