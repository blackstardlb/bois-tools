package nl.blackstardlb.bois.services

import mu.KotlinLogging
import nl.blackstardlb.bois.data.repositories.OutfitCensusRepository
import nl.blackstardlb.bois.runTesting
import nl.blackstardlb.bois.timed
import org.amshove.kluent.shouldBeLessOrEqualTo
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

private val logger = KotlinLogging.logger {}

@SpringBootTest
@ActiveProfiles("test")
internal class OutfitMemberServiceTest {
    @Autowired
    lateinit var outfitMemberService: OutfitMemberService

    @Autowired
    lateinit var outfitCensusRepository: OutfitCensusRepository

    @Test
    fun getOutfitMemberRankChangeRecommendations() = runTesting {
        val boiz = outfitCensusRepository.getOutfitForTag("BOIS").shouldNotBeNull()
        val list = timed("getOutfitMemberRankChangeRecommendations") { outfitMemberService.getOutfitMemberRankChangeRecommendations().onEach { logger.info { it } }.shouldNotBeNull() }
        list.size shouldBeLessOrEqualTo boiz.memberCount.toInt()
    }
}