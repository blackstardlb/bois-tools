package nl.blackstardlb.bois.config

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.inamik.text.tables.SimpleTable
import com.inamik.text.tables.grid.Border
import com.inamik.text.tables.grid.Util
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import nl.blackstardlb.bois.data.models.BattleRank
import nl.blackstardlb.bois.data.models.FullOutfitMember
import nl.blackstardlb.bois.data.models.OutfitCensusRepository
import nl.blackstardlb.bois.round
import nl.blackstardlb.bois.safeDivide
import nl.blackstardlb.bois.services.OutfitMemberService
import nl.blackstardlb.bois.services.ShutdownService
import nl.blackstardlb.bois.services.models.OrbitalStrikeStats
import nl.blackstardlb.bois.services.models.OutfitMemberRankRecommendation
import nl.blackstardlb.bois.timed
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.io.File
import java.lang.IllegalArgumentException
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

private val logger = KotlinLogging.logger {}

@Component
@Profile("!test")
class SpringRunner(
        val outfitMemberService: OutfitMemberService,
        val shutdownService: ShutdownService,
        val outfitCensusRepository: OutfitCensusRepository,
        @Value("\${output.file.location.recommendations}") val recommendationsLocation: String,
        @Value("\${output.file.location.orbitals}") val orbitalsLocation: String,
        @Value("\${outfits}") val outfits: String,
        @Value("\${outfit.alias}") val outfitAlias: String
) : CommandLineRunner {
    companion object {
        const val TWENTY = "20-30"
        const val THIRTY = "31-40"
        const val FORTY = "41-50"
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override fun run(vararg args: String?) = runBlocking {
        val writer: (String) -> ((List<List<String>>) -> Unit) = { csvWriter(it) }

//        recommendations(writer)
        orbitals(writer)
//        averageOrbitals(writer)

//        outfitKDAComparisons(writer)
        shutdownService.shutDown()
    }

    private suspend fun outfitKDAComparisons(writer: (String) -> (List<List<String>>) -> Unit) {
        timed("Outfit KDA comparisons") {
            val groupedOutfitMembers = outfits.split(",")
                    .map { outfitTag ->
                        val groupedMembers = outfitCensusRepository.getFullOutfitMembersForTag(outfitTag)
                                .filter { it.character.times.lastLogin.isAfter(Instant.now().minus(7, ChronoUnit.DAYS)) }
                                .filter { it.character.battleRank.value in 20..50 }
                                .groupBy { it.character.battleRank.toGroup() }
                        Pair(outfitTag, groupedMembers)
                    }
            groupedOutfitMembers.gomToListOfListOfString().write(writer("build/output/kd-comparison"))
        }
    }

    fun BattleRank.toGroup(): String {
        return when (this.value) {
            in 20..29 -> TWENTY
            in 30..39 -> THIRTY
            in 40..50 -> FORTY
            else -> throw IllegalArgumentException("${this.value} is not in a valid group")
        }
    }

    private suspend fun averageOrbitals(writer: (String) -> (List<List<String>>) -> Unit) {
        timed("Outfit orbital strike averages", logger) {
            val outfits = outfits.split(",")
                    .map {
                        val stats = outfitMemberService.getOutfitMemberOrbitalStrikeStats(it)
                        val hits = stats.fold(0L) { acc, value -> acc + value.hits }
                        val kills = stats.fold(0L) { acc, value -> acc + value.kills }
                        val strikes = stats.fold(0L) { acc, value -> acc + value.strikes }
                        val score = stats.fold(0L) { acc, value -> acc + value.score }
                        val vehicleKills = stats.fold(0L) { acc, value -> acc + value.vehicleKills }
                        return@map OrbitalStrikeStats(it, kills, vehicleKills, strikes, hits, score)
                    }
                    .sortedWith(compareBy({ it.killsPerStrike }, { it.characterName }))
                    .onEach { logger.info { it } }

            createPath(orbitalsLocation)
            outfits.orbitalsToListOfListOfStrings().write(writer(orbitalsLocation))
        }
    }

    private suspend fun orbitals(writer: (String) -> (List<List<String>>) -> Unit) {
        timed("saving orbital strike stats", logger) {
            val stats = outfitMemberService.getOutfitMemberOrbitalStrikeStats(outfitAlias).filter { it.strikes >= 5 }
                    .sortedWith(compareBy({ it.killsPerStrike }, { it.characterName }))
                    .onEach { logger.info { it } }
            createPath(orbitalsLocation)
            stats.orbitalsToListOfListOfStrings().write(writer(orbitalsLocation))
        }
    }

    private suspend fun recommendations(writer: (String) -> (List<List<String>>) -> Unit) {
        timed("saving rank recommendations", logger) {
            val recommendations = outfitMemberService.getOutfitMemberRankChangeRecommendations(outfitAlias)
                    .sortedWith(compareBy({ it.br }, { it.memberName }, { it.recommendedRank.id }))
                    .onEach { logger.info { it } }
            createPath(recommendationsLocation)
            recommendations.recommendationsToListOfListOfStrings().write(writer(recommendationsLocation))
        }
    }

    private fun createPath(pathString: String) {
        val path = Paths.get(pathString).parent
        Files.createDirectories(path)
    }

    fun List<List<String>>.write(writer: (List<List<String>>) -> Unit) {
        writer(this)
    }

    fun List<OrbitalStrikeStats>.orbitalsToListOfListOfStrings(): List<List<String>> {
        return this.map { listOf(it.characterName, it.killsPerStrike.toString(), it.vehicleKillsPerStrike.toString(), it.strikes.toString(), it.hitsPerStrike.toString(), it.scorePerStrike.toString()) }
                .toMutableList().also {
                    it.add(0, listOf("Name", "Kills/Strike", "Vehicle Kills/Strike", "Strikes", "Hits/Strike", "Score/Strike"))
                }
    }

    fun List<OutfitMemberRankRecommendation>.recommendationsToListOfListOfStrings(): List<List<String>> {
        return this.map { listOf(it.br.toString(), it.memberName, it.recommendedRank.apiName) }
    }

    fun csvWriter(file: String): (List<List<String>>) -> Unit {
        return {
            val writer = csvWriter {
                delimiter = ','
                lineTerminator = "\n"
            }
            writer.writeAll(it, "$file.csv")
        }
    }

    fun textTableWriter(file: String): (List<List<String>>) -> Unit {
        return {
            var tableString = ""
            if (it.isNotEmpty()) {
                val simpleTable = it.fold(SimpleTable.of()) { table, list ->
                    list.fold(table.nextRow()) { rowTable, cell ->
                        rowTable.nextCell(cell)
                    }
                }
                tableString = Util.asString(Border.SINGLE_LINE.apply(simpleTable.toGrid()))
            }
            File("${file}.txt").writeText(tableString)
        }
    }
}

private fun List<Pair<String, Map<String, List<FullOutfitMember>>>>.gomToListOfListOfString(): List<List<String>> {
    fun FullOutfitMember.weeksStat(stat: String): Long {
        return this.charactersStatHistory.first { it.id == stat }.week.values.sum()
//        return this.charactersStatHistory.first { it.id == stat }.day.filterKeys { it.substringAfter("d").toInt() in 1..7 }.values.sum()
    }

    fun List<FullOutfitMember>.kda(): Double {
        val kills = this.map { it.weeksStat("kills") }.sum()
        val deaths = this.map { it.weeksStat("deaths") }.sum()
        return kills.safeDivide(deaths)
    }

    fun List<FullOutfitMember>.kpm(): Double {
        val kills = this.map { it.weeksStat("kills") }.sum()
        val time = Duration.ofSeconds(this.map { it.weeksStat("time") }.sum()).toMinutes()
        return kills.safeDivide(time)
    }

//    this.forEach { c -> c.second.forEach { logger.info { "${c.first} ${it.key} ${it.value.size}" } } }

    val keyOrder = listOf(SpringRunner.TWENTY, SpringRunner.THIRTY, SpringRunner.FORTY)
    val output = this.map { c ->
        listOf(
                listOf(c.first),
                keyOrder.map { c.second[it]?.kpm()?.round(3)?.toString() ?: error("No Value for key $it") },
                keyOrder.map { c.second[it]?.kda()?.round(3)?.toString() ?: error("No Value for key $it") }
        ).flatten()
    }.toMutableList()

    output.add(0, listOf(listOf("OUTFIT"), keyOrder.map { "$it kpm" }, keyOrder.map { "$it kd" }).flatten())
    return output
}
