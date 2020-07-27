package nl.blackstardlb.bois.data.repositories

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import nl.blackstardlb.bois.data.clients.CensusClient
import nl.blackstardlb.bois.data.clients.sendRequestWithRetry
import nl.blackstardlb.bois.data.models.Character
import nl.blackstardlb.bois.data.models.CharactersStatHistory
import nl.blackstardlb.bois.data.models.Constants
import nl.blackstardlb.bois.data.models.lists.CharacterList
import nl.blackstardlb.bois.data.models.lists.CharactersStatHistoryList
import nl.blackstardlb.bois.splitList
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class CharacterCensusRepositoryImpl(val censusClient: CensusClient, @Value("\${characters.page_size}") val charactersPageSize: Int) : CharacterCensusRepository {
    override suspend fun getCharacterStatHistories(characterId: String): List<CharactersStatHistory> {
        val uriParameters = listOf(
                "character_id" to listOf(characterId),
                "c:limit" to listOf("100")
        )
        return censusClient.sendRequestWithRetry<CharactersStatHistoryList>(Constants.Endpoints.CHARACTERS_STAT_HISTORY, uriParameters).data
    }

    override suspend fun getCharactersByIds(ids: List<String>): List<Character> {
        return ids.splitList(charactersPageSize).map { GlobalScope.async { getCharactersByIdsSubList(it) } }.awaitAll().flatten()
    }

    private suspend fun getCharactersByIdsSubList(ids: List<String>): List<Character> {
        val uriParameters = listOf(
                "character_id" to ids,
                "c:limit" to listOf(charactersPageSize.toString())
        )
        return censusClient.sendRequestWithRetry<CharacterList>(Constants.Endpoints.CHARACTER, uriParameters).data
    }
}