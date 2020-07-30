package nl.blackstardlb.bois.services.models

data class OutfitMemberStats(
        val id: String,
        val name: String,
        val br: Int,
        val prestigeLevel: Int,
        val outfitRank: Int,
        val minutesPlayed: Double,
        val minutesPlayedLastTwoWeeks: Double,
        val medicPercentage: Int,
        val killsPM: Double,
        val capturesPH: Double,
        val defencesPH: Double,
        val sundererSpawnsPM: Double,
        val routerSpawnsPM: Double,
        val revivesPM: Double,
        val healsPM: Double,
        val sunderersDestroyedPM: Double
)