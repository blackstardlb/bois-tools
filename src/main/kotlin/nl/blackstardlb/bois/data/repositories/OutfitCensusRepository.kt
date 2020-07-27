package nl.blackstardlb.bois.data.repositories

import nl.blackstardlb.bois.data.models.FullOutfitMember
import nl.blackstardlb.bois.data.models.Outfit
import nl.blackstardlb.bois.data.models.OutfitMember
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface OutfitCensusRepository {
    suspend fun getOutfitForTag(tag: String): Outfit
    suspend fun getFullOutfitMember(memberId: String): FullOutfitMember
    suspend fun getFullOutfitMembersForTag(tag: String): List<FullOutfitMember>
}