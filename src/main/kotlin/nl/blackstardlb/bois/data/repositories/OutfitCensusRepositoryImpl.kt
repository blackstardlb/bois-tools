package nl.blackstardlb.bois.data.repositories

import kotlinx.coroutines.*
import mu.KotlinLogging
import nl.blackstardlb.bois.data.clients.CensusClient
import nl.blackstardlb.bois.data.clients.sendRequestWithRetry
import nl.blackstardlb.bois.data.models.*
import nl.blackstardlb.bois.data.models.lists.FullOutfitMemberList
import nl.blackstardlb.bois.data.models.lists.ItemEventList
import nl.blackstardlb.bois.data.models.lists.MemberOrbitalStrikeFactionStatsList
import nl.blackstardlb.bois.data.models.lists.OutfitList
import nl.blackstardlb.bois.services.models.OutfitRank
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
        val characterStatHistoryJoin = CensusJoin(target = Constants.Endpoints.CHARACTERS_STAT_HISTORY, on = "character_id", to = "character_id", injectAt = "characters_stat_history", isList = true, show = listOf("stat_name", "character_id", "week", "day"))
        val characterStatsPlaytimeJoin = CensusJoin(target = Constants.Endpoints.CHARACTERS_STAT, on = "character_id", to = "character_id", injectAt = "characters_play_times", isList = true, terms = listOf("stat_name=play_time"), show = listOf("stat_name", "character_id", "profile_id", "value_monthly"))
        val orbitalStrikeWeaponStatByFactionJoin = CensusJoin(target = Constants.Endpoints.CHARACTERS_WEAPON_STAT_BY_FACTION, on = "character_id", to = "character_id", injectAt = "orbital_strike_stats", isList = true, terms = listOf("item_id=${Constants.Items.ARMORY_ORBITAL_STRIKE}"), show = listOf("stat_name", "value_vs", "value_nc", "value_tr"), includeNonMatches = false)
        val orbitalStrikeWeaponStatJoin = CensusJoin(target = Constants.Endpoints.CHARACTERS_WEAPON_STAT, on = "character_id", to = "character_id", injectAt = "orbital_strike_hit_and_score_stats", isList = true, terms = listOf("item_id=${Constants.Items.ARMORY_ORBITAL_STRIKE}"), show = listOf("stat_name", "value"), includeNonMatches = false)
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
        return getOutfitForTag(tag).idAndMembers { id, totalMembers ->
            coroutineScope {
                logger.info { "Total members $totalMembers" }
                val twoWeeksAgo = Instant.now().minus(14, ChronoUnit.DAYS).epochSecond
                val characterFiltered = characterJoin.copy(terms = listOf("times.last_login=>$twoWeeksAgo"), includeNonMatches = false)
                val uriParameters = listOf(
                        "c:limit" to listOf(totalMembers.toString()),
                        "c:join" to listOf(characterFiltered.build(), characterStatHistoryJoin.build(), characterStatsPlaytimeJoin.build()),
                        "c:hide" to listOf("member_since_date", "member_since", "rank"),
                        "outfit_id" to listOf(id)
                )
                censusClient.sendRequestWithRetry<FullOutfitMemberList>(Constants.Endpoints.OUTFIT_MEMBER, uriParameters = uriParameters).data
            }
        }
    }

    override suspend fun getOutfitMemberOrbitalStrikeFactionStats(tag: String): List<MemberOrbitalStrikeFactionStats> {
        return getOutfitForTag(tag).idAndMembers { id, totalMembers ->
            coroutineScope {
                logger.info { "Total members $totalMembers" }
                val uriParameters = listOf(
                        "c:limit" to listOf(totalMembers.toString()),
                        "c:join" to listOf(characterJoin.build(), orbitalStrikeWeaponStatByFactionJoin.build(), orbitalStrikeWeaponStatJoin.build()),
                        "c:hide" to listOf("member_since_date", "member_since", "rank"),
                        "outfit_id" to listOf(id)
//                        "rank_ordinal" to listOf("<${OutfitRank.Justicar.id + 1}")
                )
                censusClient.sendRequestWithRetry<MemberOrbitalStrikeFactionStatsList>(Constants.Endpoints.OUTFIT_MEMBER, uriParameters = uriParameters).data
            }
        }
    }

    override suspend fun getCharacterItemEventsSince(characterId: String, before: Instant?): List<ItemEvent> {
        return coroutineScope {
            val maxValues = 5000
            val uriParameters = mutableListOf(
                    "c:limit" to listOf(maxValues.toString()),
                    "character_id" to listOf(characterId),
                    "type" to listOf("ITEM")
            )
            if (before != null) {
                uriParameters.add("before" to listOf(before.epochSecond.toString()))
            }
            val data = censusClient.sendRequestWithRetry<ItemEventList>(Constants.Endpoints.CHARACTERS_EVENT, uriParameters = uriParameters).data
            if (data.size == maxValues) {
                logger.info { "Fetching more data for $characterId" }
                return@coroutineScope listOf(data, getCharacterItemEventsSince(characterId, data.sortedBy { it.timestamp }.last().timestamp)).flatten()
            }
            return@coroutineScope data
        }
    }

    private suspend fun <T> Outfit.idAndMembers(block: suspend (String, Long) -> T): T {
        return block(this.id, this.memberCount)
    }
}