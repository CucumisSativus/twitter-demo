package net.cucumbersome.twitterdemo

import io.micronaut.serde.annotation.Serdeable
import javax.validation.constraints.NotBlank

@Serdeable
data class GenreUpdateCommand(
    val id: Long,
    @field:NotBlank val name: String
)