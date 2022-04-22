package benjamin.users.impl

import benjamin.users.api.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockserver.client.MockServerClient
import org.mockserver.integration.ClientAndServer
import org.mockserver.junit.jupiter.MockServerExtension
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.model.MediaType
import org.springframework.web.reactive.function.client.WebClient

@ExtendWith(MockServerExtension::class)
class UsersFetcherImplTest(private val mockServer: ClientAndServer) {
    private val webClient = WebClient.builder()
        .baseUrl("http://localhost:${mockServer.localPort}")
        .build()

    private val usersJson = """
        [
            {
                "username": "a.elmurzaev95",
                "firstName": "Adam",
                "lastName": "Elmurzaev"
            }
        ]
    """.trimIndent()

    private val users = listOf(
        User(
            username = "a.elmurzaev95",
            firstName = "Adam",
            lastName = "Elmurzaev"
        )
    )

    @Test
    fun `fetcher should return correct response`() {
        mockServer.mock()

        assertEquals(
            users,
            UsersFetcherImpl(webClient).fetchByUserName("a.elmurzaev95")
        )
    }

    private fun MockServerClient.mock() {
        this.`when`(
            HttpRequest.request()
                .withMethod("GET")
                .withPath("/users")
                .withHeader("Accept", "application/json")
                .withQueryStringParameter("username", "a.elmurzaev95")
                .withQueryStringParameter("exact", "true")
        ).respond(
            HttpResponse.response()
                .withStatusCode(200)
                .withBody(usersJson, MediaType.APPLICATION_JSON_UTF_8)
        )
    }
}
