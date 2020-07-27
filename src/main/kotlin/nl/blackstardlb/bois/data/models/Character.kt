package nl.blackstardlb.bois.data.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class Character(
        @JsonProperty("character_id") override val id: String,
        val name: Name,
        val battleRank: BattleRank,
        val prestigeLevel: Int
) : Idable