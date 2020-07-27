package nl.blackstardlb.bois.data.repositories

import nl.blackstardlb.bois.data.models.FullOutfitMember
import nl.blackstardlb.bois.data.models.Outfit
import nl.blackstardlb.bois.data.models.OutfitMember
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface OutfitCensusRepository {
    fun getOutfitForTag(tag: String): Mono<Outfit>
    fun getFullOutfitMember(memberId: String): Mono<FullOutfitMember>
    fun getFullOutfitMembersForTag(tag: String): Flux<FullOutfitMember>
}