package nl.blackstardlb.bois.data.repositories

import nl.blackstardlb.bois.data.clients.CensusClient
import nl.blackstardlb.bois.data.clients.sendRequestWithRetry
import nl.blackstardlb.bois.data.models.Character
import nl.blackstardlb.bois.data.models.CharactersStatHistory
import nl.blackstardlb.bois.data.models.Constants
import nl.blackstardlb.bois.data.models.lists.CharacterList
import nl.blackstardlb.bois.data.models.lists.CharactersStatHistoryList
import nl.blackstardlb.bois.split
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class CharacterCensusRepositoryImpl(val censusClient: CensusClient, @Value("\${characters.page_size}") val charactersPageSize: Int) : CharacterCensusRepository {
    override fun getCharacterStatHistories(characterId: String): Mono<List<CharactersStatHistory>> {
        val uriParameters = listOf(
                "character_id" to listOf(characterId),
                "c:limit" to listOf("100")
        )
        return censusClient.sendRequestWithRetry<CharactersStatHistoryList>(Constants.Endpoints.CHARACTERS_STAT_HISTORY, uriParameters).map { it.data }
    }

    override fun getCharactersByIds(ids: List<String>): Mono<List<Character>> {
        val monos = ids.split(charactersPageSize).map { getCharactersByIdsSubList(it) }
        return Flux.fromIterable(monos).flatMap { it }.collectList().map { it.flatten() }
    }

    private fun getCharactersByIdsSubList(ids: List<String>): Mono<List<Character>> {
        val uriParameters = listOf(
                "character_id" to ids,
                "c:limit" to listOf(charactersPageSize.toString())
        )
        return censusClient.sendRequestWithRetry<CharacterList>(Constants.Endpoints.CHARACTER, uriParameters).map { it.data }
    }
}