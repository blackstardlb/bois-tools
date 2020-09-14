package nl.blackstardlb.bois.data.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class CharactersStatHistory(
        @JsonProperty("stat_name") override val id: String,
        val characterId: String,
        val week: Map<String, Long>,
        val day: Map<String, Long>
) : Idable