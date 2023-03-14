package net.cucumbersome

import io.micronaut.core.type.Argument
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import net.cucumbersome.twitterdemo.GenreUpdateCommand
import net.cucumbersome.twitterdemo.domain.Genre
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@MicronautTest
class GenreControllerTest(@Client("/") val client: HttpClient) {

    fun getBearerToken(): String {
        val request = HttpRequest.POST("/login", mapOf("username" to "sherlock", "password" to "password"))
        val response = client.toBlocking().exchange(request, Argument.mapOf(String::class.java, String::class.java))
        return response.body()?.get("access_token") as String
    }

    @Test
    fun testFindNonExistingGenreReturn404() {
        val token = getBearerToken()
        val thrown = assertThrows<HttpClientResponseException> {
            val request = HttpRequest.GET<Any>("/genres/99").bearerAuth(token)
            client.toBlocking().exchange(request, Genre::class.java)
        }

        assertNotNull(thrown.response)
        assertEquals(HttpStatus.NOT_FOUND, thrown.status)
    }

    @Test
    fun testGenreCrudOperations() {
        val token = getBearerToken()
        var request = HttpRequest.POST("/genres", mapOf("name" to "DevOps")).bearerAuth(token)
        var response = client.toBlocking().exchange(request, Genre::class.java)

        assertEquals(HttpStatus.CREATED, response.status)
        assertEquals("/genres/1", response.header(HttpHeaders.LOCATION))

        request = HttpRequest.POST("/genres", mapOf("name" to "Microservices")).bearerAuth(token)
        response = client.toBlocking().exchange(request, Genre::class.java)

        assertEquals(HttpStatus.CREATED, response.status)
        assertEquals("/genres/2", response.header(HttpHeaders.LOCATION))

        val req = HttpRequest.GET<Any>("/genres/2").bearerAuth(token)
        var genre = client.toBlocking().retrieve(req, Genre::class.java)

        assertEquals("Microservices", genre.name)

        val cmdRequest = HttpRequest.PUT("/genres", GenreUpdateCommand(2, "Micro-services")).bearerAuth(token)
        response = client.toBlocking().exchange(cmdRequest)

        assertEquals(HttpStatus.NO_CONTENT, response.status())

        val req2 = HttpRequest.GET<Any>("/genres/2").bearerAuth(token)
        genre = client.toBlocking().retrieve(req2, Genre::class.java)

        assertEquals("Micro-services", genre.name)

        val req3 = HttpRequest.GET<Any>("/genres/list").bearerAuth(token)
        var genres = client.toBlocking().retrieve(req3, Argument.listOf(Genre::class.java))

        assertEquals(2, genres.size)

        request = HttpRequest.POST("/genres/ex", mapOf("name" to "Microservices")).bearerAuth(token)
        response = client.toBlocking().exchange(request)

        assertEquals(HttpStatus.NO_CONTENT, response.status)

        request = HttpRequest.GET<Map<String, String>?>("/genres/list").bearerAuth(token)
        genres = client.toBlocking().retrieve(request, Argument.listOf(Genre::class.java))

        assertEquals(2, genres.size)

        request = HttpRequest.GET<Map<String, String>?>("/genres/list?size=1").bearerAuth(token)
        genres = client.toBlocking().retrieve(request, Argument.listOf(Genre::class.java))

        assertEquals(1, genres.size)
        assertEquals("DevOps", genres[0].name)

        request = HttpRequest.GET<Map<String, String>?>("/genres/list?size=1&sort=name,desc").bearerAuth(token)
        genres = client.toBlocking().retrieve(request, Argument.listOf(Genre::class.java))

        assertEquals(1, genres.size)
        assertEquals("Micro-services", genres[0].name)

        request = HttpRequest.GET<Map<String, String>?>("/genres/list?size=1&page=2").bearerAuth(token)
        genres = client.toBlocking().retrieve(request, Argument.listOf(Genre::class.java))

        assertEquals(0, genres.size)

        for (i in 1..2) {
            request = HttpRequest.DELETE<Map<String, String>?>("/genres/$i").bearerAuth(token)
            response = client.toBlocking().exchange(request)

            assertEquals(HttpStatus.NO_CONTENT, response.status)
        }

        request = HttpRequest.GET<Map<String, String>?>("/genres/list").bearerAuth(token)
        genres = client.toBlocking().retrieve(request, Argument.listOf(Genre::class.java))

        assertEquals(0, genres.size)

    }

}