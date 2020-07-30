package nl.blackstardlb.bois.data.models

import java.time.Instant

interface OutfitCensusRepository {
    suspend fun getOutfitForTag(tag: String): Outfit
    suspend fun getFullOutfitMember(memberId: String): FullOutfitMember
    suspend fun getFullOutfitMembersForTag(tag: String): List<FullOutfitMember>
    suspend fun getOutfitMemberOrbitalStrikeFactionStats(tag: String): List<MemberOrbitalStrikeFactionStats>
    suspend fun getCharacterItemEventsSince(characterId: String, before: Instant?): List<ItemEvent>
}