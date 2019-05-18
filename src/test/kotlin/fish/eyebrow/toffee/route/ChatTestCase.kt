package fish.eyebrow.toffee.route

import fish.eyebrow.toffee.App.toffeeCommon
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ChatTestCase {
    @Test
    internal fun `should respond with a 200 OK when making a GET request to chat endpoint`() {
        withTestApplication({ toffeeCommon() }) {
            handleRequest(HttpMethod.Get, "/chat").apply {
                assertThat(this.response.status()).isEqualTo(HttpStatusCode.OK)
            }
        }
    }
}