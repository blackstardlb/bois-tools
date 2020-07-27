package nl.blackstardlb.bois.data.repositories

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
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.math.ceil

private val logger = KotlinLogging.logger {}

@Component
class OutfitCensusRepositoryImpl(
        val censusClient: CensusClient,
        @Value("\${outfit_member.full_page_size}") val outfitMemberFullPageSize: Int
) : OutfitCensusRepository {
    companion object {
        val characterJoin = CensusJoin(target = Constants.Endpoints.CHARACTER, on = "character_id", to = "character_id", injectAt = "character", show = listOf("name", "battle_rank", "character_id", "prestige_level"))
        val characterStatHistory = CensusJoin(target = Constants.Endpoints.CHARACTERS_STAT_HISTORY, on = "character_id", to = "character_id", injectAt = "characters_stat_history", isList = true, show = listOf("stat_name", "character_id", "week"))
        val characterStatsPlaytime = CensusJoin(target = Constants.Endpoints.CHARACTERS_STAT, on = "character_id", to = "character_id", injectAt = "characters_play_times", isList = true, terms = listOf("stat_name=play_time"), show = listOf("stat_name", "character_id", "profile_id", "value_monthly"))
    }

    override fun getOutfitForTag(tag: String): Mono<Outfit> {
        return censusClient.sendRequestWithRetry<OutfitList>(Constants.Endpoints.OUTFIT, uriParameters = listOf("alias" to listOf(tag))).flatMap { Mono.justOrEmpty(it.data.firstOrNull()) }
    }

    override fun getFullOutfitMember(memberId: String): Mono<FullOutfitMember> {
        val uriParameters = listOf(
                "character_id" to listOf(memberId),
                "c:join" to listOf(characterJoin.build(), characterStatHistory.build(), characterStatsPlaytime.build())
        )
        return censusClient.sendRequestWithRetry<FullOutfitMemberList>(Constants.Endpoints.OUTFIT_MEMBER, uriParameters = uriParameters).flatMap { Mono.justOrEmpty(it.data.firstOrNull()) }

    }

    override fun getFullOutfitMembersForTag(tag: String): Flux<FullOutfitMember> {
        return getOutfitForTag(tag).flatMapMany { getFullOutfitMembersForOutfit(it) }
    }

    private fun getFullOutfitMembersForOutfit(outfit: Outfit): Flux<FullOutfitMember> {
        return getFullOutfitMembers(outfit.id, outfit.memberCount)
    }

    private fun getFullOutfitMembers(outfitId: String, totalMembers: Long): Flux<FullOutfitMember> {
        logger.info { "Total members $totalMembers" }
        var items = 0
        val fluxes = (0 until (ceil(totalMembers / outfitMemberFullPageSize.toDouble())).toInt()).map { page ->
            getFullOutFitMembersPage(outfitId, page).doOnNext {
                items += it.size
                logger.info { "Got page $page with ${it.size} items total $items " }
            }.flatMapIterable { it }
        }
        return Flux.merge(fluxes)
    }

    private fun getFullOutFitMembersPage(outfitId: String, page: Int): Mono<List<FullOutfitMember>> {
        val uriParameters = listOf(
                "c:limit" to listOf(outfitMemberFullPageSize.toString()),
                "outfit_id" to listOf(outfitId),
                "c:start" to listOf((page * outfitMemberFullPageSize).toString()),
                "c:join" to listOf(characterJoin.build(), characterStatHistory.build(), characterStatsPlaytime.build()),
                "c:hide" to listOf("member_since_date", "member_since", "rank")
        )
        return Mono.fromSupplier { censusClient.sendRequestWithRetry<FullOutfitMemberList>(Constants.Endpoints.OUTFIT_MEMBER, uriParameters = uriParameters).map { it.data }.block() }
    }
}