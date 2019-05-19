package fish.eyebrow.toffee.route

import com.google.gson.Gson
import com.google.gson.JsonObject
import fish.eyebrow.toffee.App.toffeeCommon
import fish.eyebrow.toffee.model.ChatModel
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.Session
import org.hibernate.Transaction
import org.hibernate.query.Query
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException

internal class ChatTestCase {
    @MockK
    internal lateinit var sessionMock: Session

    @MockK
    internal lateinit var queryMock: Query<ChatModel>

    @MockK
    internal lateinit var transactionMock: Transaction

    @BeforeEach
    internal fun setUp() {
        MockKAnnotations.init(this)

        Chat.javaClass.getDeclaredField("session").apply {
            trySetAccessible()
            set(Chat.javaClass, sessionMock)
        }

        every { sessionMock.createQuery(any<String>()) } returns queryMock
        every { sessionMock.saveOrUpdate(any<String>()) } returns Unit
        every { sessionMock.transaction } returns transactionMock
        every { sessionMock.load<ChatModel>(ChatModel::class.java, any()) } throws IllegalArgumentException()
        every { queryMock.setMaxResults(any()) } returns queryMock
        every { queryMock.resultList } returns listOf()
        every { transactionMock.begin() } returns Unit
        every { transactionMock.commit() } returns Unit
    }

    @Test
    internal fun `should invoke hibernate session create query and not throw when calling GET chat`() {
        withTestApplication({ toffeeCommon() }) {
            handleRequest(HttpMethod.Get, "/chat")

            verify(exactly = 1) { sessionMock.createQuery(any<String>()) }
        }
    }

    @Test
    internal fun `should only limit max results of chat GET query when given newest equals true`() {
        withTestApplication({ toffeeCommon() }) {
            handleRequest(HttpMethod.Get, "/chat?newest=true")

            verify(exactly = 1) { queryMock.setMaxResults(1) }
        }
    }

    @Test
    internal fun `should set max results to max integer value when not given newest param`() {
        withTestApplication({ toffeeCommon() }) {
            handleRequest(HttpMethod.Get, "/chat")

            verify(exactly = 1) { queryMock.setMaxResults(Int.MAX_VALUE) }
        }
    }

    @Test
    internal fun `should respond with json of all obtained chat models when the result list is not empty`() {
        val expectedResponseString = JsonObject().apply {
            add("chats", Gson().toJsonTree(listOf(ChatModel(), ChatModel())))
        }.toString()

        every { queryMock.resultList } returns listOf(ChatModel(), ChatModel())

        withTestApplication({ toffeeCommon() }) {
            handleRequest(HttpMethod.Get, "/chat").apply {
                assertThat(response.content).isEqualTo(expectedResponseString)
            }
        }
    }

    @Test
    internal fun `should respond with json object with no array when result is a single item`() {
        val expectedResponseString = Gson().toJson(ChatModel())

        every { queryMock.resultList } returns listOf(ChatModel())

        withTestApplication({ toffeeCommon() }) {
            handleRequest(HttpMethod.Get, "/chat").apply {
                assertThat(response.content).isEqualTo(expectedResponseString)
            }
        }
    }

    @Test
    internal fun `should respond with a 200 when result list of query is populated`() {
        every { queryMock.resultList } returns listOf(ChatModel())

        withTestApplication({ toffeeCommon() }) {
            handleRequest(HttpMethod.Get, "/chat").apply {
                assertThat(this.response.status()).isEqualTo(HttpStatusCode.OK)
            }
        }
    }

    @Test
    internal fun `should respond with 204 when result from query returns empty list`() {
        every { queryMock.resultList } returns emptyList()

        withTestApplication({ toffeeCommon() }) {
            handleRequest(HttpMethod.Get, "/chat").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NoContent)
            }
        }
    }

    @Test
    internal fun `should invoke saveOrUpdate on session when posting new chat`() {
        withTestApplication({ toffeeCommon() }) {
            handleRequest(HttpMethod.Post, "/chat") {
                setBody("""{ "text": "Hello, World!", "author": "Alexander Johnston" }""")
            }

            verify(exactly = 1) { sessionMock.saveOrUpdate(any()) }
        }
    }

    @Test
    internal fun `should invoke saveOrUpdate on session when updating chat`() {
        every { sessionMock.load(any(), any()) } returns ChatModel()

        withTestApplication({ toffeeCommon() }) {
            handleRequest(HttpMethod.Post, "/chat?id=10") {
                setBody("""{ "text": "Hello, World!", "author": "Alexander Johnston" }""")
            }
            handleRequest(HttpMethod.Post, "/chat?id=10") {
                setBody("""{ "text": "This was overwritten", "author": "Alexander Johnston" }""")
            }

            verify(exactly = 2) { sessionMock.saveOrUpdate(any()) }
        }
    }

    @Test
    internal fun `should respond with a 400 when no body is given on post`() {
        withTestApplication({ toffeeCommon() }) {
            handleRequest(HttpMethod.Post, "/chat").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
            }
        }
    }
}