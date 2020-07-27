package nl.blackstardlb.bois.services.models

data class OutfitMemberRankRecommendation(val memberName: String,
                                          val br: Int,
                                          val currentRank: OutfitRank,
                                          val recommendedRank: OutfitRank) {
    val shouldRankBeChanged: Boolean = currentRank != recommendedRank
}