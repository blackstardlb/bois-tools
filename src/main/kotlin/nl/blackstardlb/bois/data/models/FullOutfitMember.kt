package nl.blackstardlb.bois.data.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class FullOutfitMember(
        @JsonProperty("character_id") override val id: String,
        val rankOrdinal: Int,
        val outfitId: String,
        val character: Character,
        val charactersStatHistory: List<CharactersStatHistory>,
        val charactersPlayTimes: List<CharacterStat>
) : Idable