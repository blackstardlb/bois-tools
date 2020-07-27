package nl.blackstardlb.bois.data.repositories

import nl.blackstardlb.bois.data.models.Character
import nl.blackstardlb.bois.data.models.CharactersStatHistory
import reactor.core.publisher.Mono

interface CharacterCensusRepository {
    suspend fun getCharacterStatHistories(characterId: String): List<CharactersStatHistory>
    suspend fun getCharactersByIds(ids: List<String>): List<Character>
}