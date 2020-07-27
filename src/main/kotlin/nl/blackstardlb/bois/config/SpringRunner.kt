package nl.blackstardlb.bois.config

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.inamik.text.tables.SimpleTable
import com.inamik.text.tables.grid.Border
import com.inamik.text.tables.grid.Util
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import nl.blackstardlb.bois.services.OutfitMemberService
import nl.blackstardlb.bois.services.ShutdownService
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
        @Value("\${output.file.location}") val outputFileLocation: String
) : CommandLineRunner {
    override fun run(vararg args: String?) = runBlocking {
        @Suppress("BlockingMethodInNonBlockingContext")
        timed("saving rank recommendations", logger) {
            val recommendations = outfitMemberService.getOutfitMemberRankChangeRecommendations()
                    .onEach { logger.info { it } }
                    .sortedWith(compareBy({ it.br }, { it.memberName }, { it.recommendedRank.id }))
                    .map {
                        it
                    }

            val path = Paths.get(outputFileLocation).parent
            Files.createDirectories(path)
            writeToCSV(outputFileLocation, recommendations)
        }
        shutdownService.shutDown()
    }

    private fun SimpleTable.addRecommendation(recommendation: OutfitMemberRankRecommendation): SimpleTable {
        return this.nextRow().nextCell(recommendation.br.toString()).nextCell(recommendation.memberName).nextCell(recommendation.recommendedRank.apiName)
    }

    private fun toTextTable(recommendations: List<OutfitMemberRankRecommendation>): String {
        val initTable = SimpleTable.of().nextRow().nextCell("BR").nextCell("Name").nextCell("New Rank")
        val table = recommendations
                .fold(initTable) { table, recommendation -> table.addRecommendation(recommendation) }

        return Util.asString(Border.SINGLE_LINE.apply(table.toGrid()))
    }

    private fun toCSV(recommendations: List<OutfitMemberRankRecommendation>): List<List<String>> {
        return recommendations.map { listOf(it.br.toString(), it.memberName, it.recommendedRank.apiName) }
    }

    private suspend fun writeToCSV(file: String, recommendations: List<OutfitMemberRankRecommendation>) {
        val writer = csvWriter {
            delimiter = ','
            lineTerminator = "\n"
        }
        writer.writeAll(toCSV(recommendations), "$file.csv")
    }

    private suspend fun writeToTextTable(file: String, recommendations: List<OutfitMemberRankRecommendation>) {
        val aFile = File("${file}.txt")
        aFile.writeText(toTextTable(recommendations))
    }
}