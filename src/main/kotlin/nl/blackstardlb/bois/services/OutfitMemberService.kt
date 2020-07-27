package nl.blackstardlb.bois.services

import mu.KotlinLogging
import nl.blackstardlb.bois.data.models.Constants
import nl.blackstardlb.bois.data.models.FullOutfitMember
import nl.blackstardlb.bois.data.repositories.OutfitCensusRepository
import nl.blackstardlb.bois.services.models.OutfitMemberRankRecommendation
import nl.blackstardlb.bois.services.models.OutfitMemberStats
import nl.blackstardlb.bois.services.models.OutfitRank
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class OutfitMemberService(
        val outfitCensusRepository: OutfitCensusRepository
) {
    suspend fun getOutfitMemberStats(memberId: String): OutfitMemberStats {
        return outfitCensusRepository.getFullOutfitMember(memberId).toOutFitMembersStats()
    }

    suspend fun getOutfitMemberRankChangeRecommendations(): List<OutfitMemberRankRecommendation> {
        return outfitCensusRepository.getFullOutfitMembersForTag("BOIS").filter { it.rankOrdinal > OutfitRank.Guardian.id }.map { it.toOutFitMembersStats().toRecommendation() }.filter { it.shouldRankBeChanged }
    }

    fun FullOutfitMember.toOutFitMembersStats(): OutfitMemberStats {
        val captures = this.charactersStatHistory.first { it.id == "facility_capture" }.week.values.sum()
        val kills = this.charactersStatHistory.first { it.id == "kills" }.week.values.sum()
        val defends = this.charactersStatHistory.first { it.id == "facility_defend" }.week.values.sum()
        val minutesPlayed = this.charactersStatHistory.first { it.id == "time" }.week.values.sum() / 60.0

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

    private fun Long.safeDivide(long: Long): Double {
        if (long == 0L) return 0.0
        return this / (long.toDouble())
    }


    private fun Long.safeDivide(double: Double): Double {
        if (double == 0.0) return 0.0
        return this / (double)
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
