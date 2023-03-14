package net.cucumbersome

import io.micronaut.data.exceptions.DataAccessException
import io.micronaut.data.model.Pageable
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.Status
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import net.cucumbersome.twitterdemo.GenreRepository
import net.cucumbersome.twitterdemo.GenreUpdateCommand
import net.cucumbersome.twitterdemo.domain.Genre
import java.net.URI
import java.util.Optional
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@ExecuteOn(TaskExecutors.IO)
@Controller("/genres")
@Secured(SecurityRule.IS_AUTHENTICATED)
open class GenreController(private val genreRepository: GenreRepository) {


    @Get("/{id}")
    fun show(id:Long): Optional<Genre> =
        genreRepository.findById(id)

    @Put
    open fun update(@Body @Valid command: GenreUpdateCommand): HttpResponse<Genre> {
        val id = genreRepository.update(command.id, command.name)

        return HttpResponse
            .noContent<Genre>()
            .header(HttpHeaders.LOCATION, id.location.path)
    }

    @Get("/list")
    open fun list(@Valid pageable: Pageable): List<Genre> =
        genreRepository.findAll(pageable).content


    @Post
    open fun save(@Body("name") @NotBlank name: String) : HttpResponse<Genre> {
        val genre = genreRepository.save(name)

        return HttpResponse
            .created(genre)
            .headers { headers -> headers.location(genre.location) }
    }

    @Post("/ex")
    open fun saveExceptions(@Body @NotBlank name: String): HttpResponse<Genre> {
        return try {
            val genre = genreRepository.saveWithException(name)

            HttpResponse
                .created(genre)
                .headers { headers -> headers.location(genre.location) }
        } catch (ex: DataAccessException) {
            HttpResponse.noContent()
        }
    }

    @Delete("/{id}")
    @Status(HttpStatus.NO_CONTENT)
    fun delete(id: Long) = genreRepository.deleteById(id)

    private val Long?.location : URI
        get() = URI.create("/genres/$this")

    private val Genre.location : URI
        get() = id.location
}