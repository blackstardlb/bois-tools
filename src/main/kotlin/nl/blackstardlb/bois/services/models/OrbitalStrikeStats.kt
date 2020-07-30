package nl.blackstardlb.bois.services.models

import nl.blackstardlb.bois.round
import nl.blackstardlb.bois.safeDivide

data class OrbitalStrikeStats(val characterName: String, val kills: Long, val vehicleKills: Long, val strikes: Long, val hits: Long, val score: Long) {
    val killsPerStrike = kills.safeDivide(strikes).round()
    val vehicleKillsPerStrike = vehicleKills.safeDivide(strikes).round()
    val hitsPerStrike = hits.safeDivide(strikes).round()
    val scorePerStrike = score.safeDivide(strikes).round()
}