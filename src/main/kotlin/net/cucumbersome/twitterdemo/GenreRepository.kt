package net.cucumbersome.twitterdemo

import io.micronaut.data.annotation.Id
import io.micronaut.data.exceptions.DataAccessException
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.PageableRepository
import net.cucumbersome.twitterdemo.domain.Genre
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@JdbcRepository(dialect = Dialect.POSTGRES)
abstract class GenreRepository : PageableRepository<Genre, Long> {

    abstract fun save(@NotBlank name: String) : Genre

    @Transactional
    fun saveWithException(@NotBlank name: String): Genre {
        save(name)
        throw DataAccessException("test exception")
    }

    abstract fun update(@Id id: Long, @NotBlank name: String) : Long
}