package nl.blackstardlb.bois.config

import com.inamik.text.tables.SimpleTable
import mu.KotlinLogging
import nl.blackstardlb.bois.services.OutfitMemberService
import nl.blackstardlb.bois.services.ShutdownService
import nl.blackstardlb.bois.services.models.OutfitMemberRankRecommendation
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
@Profile("!test")
class SpringRunner(
        val outfitMemberService: OutfitMemberService,
        val shutdownService: ShutdownService
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        val table = outfitMemberService.getOutfitMemberRankChangeRecommendations()
                .doOnNext { logger.info { it } }
                .collectList()
                .map { it.sortedBy { recommendation -> recommendation.recommendedRank.id } }
                .map {
                    val initTable = SimpleTable.of().nextRow().nextCell("Name").nextCell("New Rank").nextCell("Current Rank")
                    it.fold(initTable) { table, recommendation -> table.addRecommendation(recommendation) }
                }

        table.doFinally { shutdownService.shutDown() }.block()
    }

    fun SimpleTable.addRecommendation(recommendation: OutfitMemberRankRecommendation): SimpleTable {
        return this.nextRow().nextCell(recommendation.memberName).nextCell(recommendation.recommendedRank.name).nextCell(recommendation.currentRank.name)
    }
}