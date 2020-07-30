package nl.blackstardlb.bois.config

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.inamik.text.tables.SimpleTable
import com.inamik.text.tables.grid.Border
import com.inamik.text.tables.grid.Util
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
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
import java.nio.file.Files
import java.nio.file.Paths

private val logger = KotlinLogging.logger {}

@Component
@Profile("!test")
class SpringRunner(
        val outfitMemberService: OutfitMemberService,
        val shutdownService: ShutdownService,
        @Value("\${output.file.location.recommendations}") val recommendationsLocation: String,
        @Value("\${output.file.location.orbitals}") val orbitalsLocation: String
) : CommandLineRunner {
    @Suppress("BlockingMethodInNonBlockingContext")
    override fun run(vararg args: String?) = runBlocking {
        val writer: (String) -> ((List<List<String>>) -> Unit) = { textTableWriter(it) }

        recommendations(writer)
        orbitals(writer)
//        averageOrbitals(writer)

        shutdownService.shutDown()
    }

    private suspend fun averageOrbitals(writer: (String) -> (List<List<String>>) -> Unit) {
        timed("Outfit orbital stike avarages", logger) {
            val outfits = listOf("PSET", "GMSY", "TRID", "TFDN", "URGE", "T", "BOIS")
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
            val stats = outfitMemberService.getOutfitMemberOrbitalStrikeStats().filter { it.strikes >= 5 }
                    .sortedWith(compareBy({ it.killsPerStrike }, { it.characterName }))
                    .onEach { logger.info { it } }
            createPath(orbitalsLocation)
            stats.orbitalsToListOfListOfStrings().write(writer(orbitalsLocation))
        }
    }

    private suspend fun recommendations(writer: (String) -> (List<List<String>>) -> Unit) {
        timed("saving rank recommendations", logger) {
            val recommendations = outfitMemberService.getOutfitMemberRankChangeRecommendations()
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