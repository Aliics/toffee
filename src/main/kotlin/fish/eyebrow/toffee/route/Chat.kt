package fish.eyebrow.toffee.route

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get

object Chat {
    fun Route.chat() {
        get("/chat") {
            call.respond("Hello, World!")
        }
    }
}