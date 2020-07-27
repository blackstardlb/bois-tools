package nl.blackstardlb.bois.data.repositories

import nl.blackstardlb.bois.data.models.Character
import nl.blackstardlb.bois.data.models.CharactersStatHistory
import reactor.core.publisher.Mono

interface CharacterCensusRepository {
    fun getCharacterStatHistories(characterId: String): Mono<List<CharactersStatHistory>>
    fun getCharactersByIds(ids: List<String>): Mono<List<Character>>
}