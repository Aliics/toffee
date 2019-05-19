package fish.eyebrow.toffee.route

import com.google.gson.Gson
import com.google.gson.JsonObject
import fish.eyebrow.toffee.model.ChatModel
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import org.hibernate.Session
import org.hibernate.cfg.Configuration
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Chat {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private var session: Session = Configuration().configure().buildSessionFactory().openSession()

    fun Route.chat() {
        get("/chat/{newest?}") {
            val newest: Boolean = call.parameters["newest"]?.toBoolean() ?: false
            val chatQuery = session.createQuery("FROM ChatModel ORDER BY timestamp DESC").apply {
                maxResults = if (newest) 1 else Int.MAX_VALUE
            }

            val queryResults = chatQuery.resultList

            logger.info("Responding to GET /chat request with [$queryResults]")

            call.respond(
                when {
                    queryResults.size > 1 -> JsonObject().apply {
                        add(
                            "chats",
                            Gson().toJsonTree(queryResults)
                        )
                    }.toString()
                    queryResults.size == 1 -> Gson().toJsonTree(queryResults[0]).toString()
                    else -> null
                } ?: HttpStatusCode.NoContent
            )
        }
        post("/chat/{id?}") {
            val id = call.parameters["id"]?.toInt()
            val body = call.receiveText()

            if (body.isBlank()) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val chatModel = Gson().fromJson(body, ChatModel::class.java).apply {
                this.id = id ?: this.id
            }

            logger.info("Upserting to chat table from body [$body]")

            val transaction = session.transaction

            try {
                transaction.begin()

                val savedChatModel = try {
                    session.load(ChatModel::class.java, id).apply {
                        if (this != null) {
                            text = chatModel.text
                            author = chatModel.author
                        }
                    }
                } catch (e: Exception) {
                    null
                }

                session.saveOrUpdate(savedChatModel ?: chatModel)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
            } finally {
                transaction.commit()
            }

            call.respond(HttpStatusCode.OK)
        }
    }
}