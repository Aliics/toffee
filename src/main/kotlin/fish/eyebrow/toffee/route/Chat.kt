package fish.eyebrow.toffee.route

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import org.hibernate.Session
import org.hibernate.cfg.Configuration

object Chat {
    private var session: Session = Configuration().configure().buildSessionFactory().openSession()

    fun Route.chat() {
        get("/chat/{newest?}") {
            val newest: Boolean = call.parameters["newest"]?.toBoolean() ?: false
            val chatQuery = session.createQuery("FROM ChatModel ORDER BY timestamp")
            chatQuery.maxResults = if (newest) 1 else Int.MAX_VALUE

            val queryResults = chatQuery.resultList

            call.respond(when {
                queryResults.size > 1 -> JsonObject().apply {
                    add(
                        "chats",
                        Gson().toJsonTree(queryResults)
                    )
                }.toString()
                queryResults.size == 1 -> Gson().toJsonTree(queryResults[0]).toString()
                else -> null
            } ?: HttpStatusCode.NoContent)
        }
    }
}