package nl.blackstardlb.bois.data.repositories

import kotlinx.coroutines.*
import mu.KotlinLogging
import nl.blackstardlb.bois.data.clients.CensusClient
import nl.blackstardlb.bois.data.clients.sendRequestWithRetry
import nl.blackstardlb.bois.data.models.CensusJoin
import nl.blackstardlb.bois.data.models.Constants
import nl.blackstardlb.bois.data.models.FullOutfitMember
import nl.blackstardlb.bois.data.models.Outfit
import nl.blackstardlb.bois.data.models.lists.FullOutfitMemberList
import nl.blackstardlb.bois.data.models.lists.OutfitList
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

private val logger = KotlinLogging.logger {}

@Component
class OutfitCensusRepositoryImpl(
        val censusClient: CensusClient,
        @Value("\${outfit_member.full_page_size}") val outfitMemberFullPageSize: Int
) : OutfitCensusRepository {
    companion object {
        val characterJoin = CensusJoin(target = Constants.Endpoints.CHARACTER, on = "character_id", to = "character_id", injectAt = "character", show = listOf("name", "battle_rank", "character_id", "prestige_level", "times"))
        val characterStatHistoryJoin = CensusJoin(target = Constants.Endpoints.CHARACTERS_STAT_HISTORY, on = "character_id", to = "character_id", injectAt = "characters_stat_history", isList = true, show = listOf("stat_name", "character_id", "week"))
        val characterStatsPlaytimeJoin = CensusJoin(target = Constants.Endpoints.CHARACTERS_STAT, on = "character_id", to = "character_id", injectAt = "characters_play_times", isList = true, terms = listOf("stat_name=play_time"), show = listOf("stat_name", "character_id", "profile_id", "value_monthly"))
    }

    override suspend fun getOutfitForTag(tag: String): Outfit {
        return censusClient.sendRequestWithRetry<OutfitList>(Constants.Endpoints.OUTFIT, uriParameters = listOf("alias" to listOf(tag))).data.first()
    }

    override suspend fun getFullOutfitMember(memberId: String): FullOutfitMember {
        val uriParameters = listOf(
                "character_id" to listOf(memberId),
                "c:join" to listOf(characterJoin.build(), characterStatHistoryJoin.build(), characterStatsPlaytimeJoin.build())
        )
        return censusClient.sendRequestWithRetry<FullOutfitMemberList>(Constants.Endpoints.OUTFIT_MEMBER, uriParameters = uriParameters).data.first()
    }

    override suspend fun getFullOutfitMembersForTag(tag: String): List<FullOutfitMember> {
        return getFullOutfitMembersForOutfit(getOutfitForTag(tag))
    }

    private suspend fun getFullOutfitMembersForOutfit(outfit: Outfit): List<FullOutfitMember> {
        return getFullOutfitMembers(outfit.id, outfit.memberCount)
    }

    private suspend fun getFullOutfitMembers(outfitId: String, totalMembers: Long): List<FullOutfitMember> {
        return coroutineScope {
            logger.info { "Total members $totalMembers" }
            val twoWeeksAgo = Instant.now().minus(14, ChronoUnit.DAYS).toEpochMilli() / 1000
            val characterFiltered = characterJoin.copy(terms = listOf("times.last_login=>$twoWeeksAgo"), includeNonMatches = false)
            val uriParameters = listOf(
                    "c:limit" to listOf(totalMembers.toString()),
                    "c:join" to listOf(characterFiltered.build(), characterStatHistoryJoin.build(), characterStatsPlaytimeJoin.build()),
                    "c:hide" to listOf("member_since_date", "member_since", "rank"),
                    "outfit_id" to listOf(outfitId)
            )
            censusClient.sendRequestWithRetry<FullOutfitMemberList>(Constants.Endpoints.OUTFIT_MEMBER, uriParameters = uriParameters).data
        }
    }
}