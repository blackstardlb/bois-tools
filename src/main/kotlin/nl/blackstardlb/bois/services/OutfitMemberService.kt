package nl.blackstardlb.bois.services

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging
import nl.blackstardlb.bois.data.models.Constants
import nl.blackstardlb.bois.data.models.FullOutfitMember
import nl.blackstardlb.bois.data.models.OutfitCensusRepository
import nl.blackstardlb.bois.safeDivide
import nl.blackstardlb.bois.services.models.OrbitalStrikeStats
import nl.blackstardlb.bois.services.models.OutfitMemberRankRecommendation
import nl.blackstardlb.bois.services.models.OutfitMemberStats
import nl.blackstardlb.bois.services.models.OutfitRank
import org.springframework.stereotype.Service
import java.time.Duration

private val logger = KotlinLogging.logger {}

@Service
class OutfitMemberService(
        val outfitCensusRepository: OutfitCensusRepository
) {
    suspend fun getOutfitMemberStats(memberId: String): OutfitMemberStats {
        return outfitCensusRepository.getFullOutfitMember(memberId).toOutFitMembersStats()
    }

    suspend fun getOutfitMemberRankChangeRecommendations(): List<OutfitMemberRankRecommendation> {
        return outfitCensusRepository.getFullOutfitMembersForTag("BOIS")
                .asSequence()
                .filter { it.rankOrdinal > OutfitRank.Guardian.id }
                .map { it.toOutFitMembersStats() }
                .filter { it.minutesPlayedLastTwoWeeks >= Duration.ofHours(2).toMinutes() }
                .map { it.toRecommendation() }
                .filter { it.shouldRankBeChanged }
                .toList()
    }

    suspend fun getOutfitMemberOrbitalStrikeStats(): List<OrbitalStrikeStats> {
        return getOutfitMemberOrbitalStrikeStats("BOIS")
    }

    suspend fun getOutfitMemberOrbitalStrikeStats(tag: String): List<OrbitalStrikeStats> {
        return outfitCensusRepository.getOutfitMemberOrbitalStrikeFactionStats(tag).asFlow()
                .flatMapMerge(concurrency = 5) { strikeFactionStats ->
                    flow {
                        val characterItemEvents = outfitCensusRepository.getCharacterItemEventsSince(strikeFactionStats.id, null)
                        val orbitals = characterItemEvents.filter { it.itemId == Constants.Items.ORBITAL_STRIKE }
                        val weaponKills = strikeFactionStats.orbitalStrikeStats.firstOrNull { it.id == "weapon_kills" }
                        val kills = if (weaponKills == null) 0 else weaponKills.valueNc + weaponKills.valueTr + weaponKills.valueVs
                        val hits = strikeFactionStats.orbitalStrikeHitAndScoreStats.firstOrNull { it.id == "weapon_hit_count" }?.value ?: 0L
                        val score = strikeFactionStats.orbitalStrikeHitAndScoreStats.firstOrNull { it.id == "weapon_score" }?.value ?: 0L
                        val vehicleWeaponKills = strikeFactionStats.orbitalStrikeStats.firstOrNull { it.id == "weapon_vehicle_kills" }
                        val vehicleKills = if (vehicleWeaponKills == null) 0 else vehicleWeaponKills.valueNc + vehicleWeaponKills.valueTr + vehicleWeaponKills.valueVs
                        emit(OrbitalStrikeStats(strikeFactionStats.character.name.first, kills, vehicleKills, orbitals.size.toLong(), hits, score))
                    }
                }.toList()
    }

    fun FullOutfitMember.toOutFitMembersStats(): OutfitMemberStats {
        val captures = this.charactersStatHistory.first { it.id == "facility_capture" }.week.values.sum()
        val kills = this.charactersStatHistory.first { it.id == "kills" }.week.values.sum()
        val defends = this.charactersStatHistory.first { it.id == "facility_defend" }.week.values.sum()
        val timeWeek = this.charactersStatHistory.first { it.id == "time" }.week
        val minutesPlayed = timeWeek.values.sum() / 60.0
        val minutesPlayedLastTwoWeeks = ((timeWeek["w1"] ?: 0) + (timeWeek["w2"] ?: 0)) / 60.0

        val monthlyMedicTime = this.charactersPlayTimes.firstOrNull { it.profileId == Constants.Class.COMBAT_MEDIC }?.valueMonthly
                ?: 0L
        val monthlyClassTime = this.charactersPlayTimes.map { it.valueMonthly }.sum()
        val medicPercentage = monthlyMedicTime.safeDivide(monthlyClassTime / 100).toInt()

        return OutfitMemberStats(
                this.id,
                this.character.name.first,
                this.character.battleRank.value,
                this.character.prestigeLevel,
                this.rankOrdinal,
                minutesPlayed,
                minutesPlayedLastTwoWeeks,
                medicPercentage,
                kills.safeDivide(minutesPlayed),
                captures.safeDivide(minutesPlayed / 60.0),
                defends.safeDivide(minutesPlayed / 60.0),
                0.0,
                0.0,
                0.0,
                0.0,
                0.0
        )
    }

    fun OutfitMemberStats.toRecommendation(): OutfitMemberRankRecommendation {
        val currentRank = OutfitRank.values().firstOrNull { it.id == this.outfitRank } ?: OutfitRank.TruthSeeker
        val newRank = OutfitRank.values().filter { it.isApplicable(this) }.minBy { it.id } ?: OutfitRank.TruthSeeker
        return OutfitMemberRankRecommendation(
                this.name,
                this.br,
                currentRank,
                newRank
        )
    }

}
