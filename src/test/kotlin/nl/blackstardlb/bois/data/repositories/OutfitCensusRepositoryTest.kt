package nl.blackstardlb.bois.data.repositories

import mu.KotlinLogging
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be empty`
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

private val logger = KotlinLogging.logger {}

@SpringBootTest
@ActiveProfiles("test")
internal class OutfitCensusRepositoryTest {
    @Autowired
    lateinit var outfitCensusRepository: OutfitCensusRepository

    @Test
    fun getOutfitForTag() {
        val bois = outfitCensusRepository.getOutfitForTag("BOIS").block().shouldNotBeNull()
        bois.alias `should be equal to` "BOIS"
    }

    @Test
    fun getMembersForTag() {
        val bois = outfitCensusRepository.getFullOutfitMembersForTag("BOIS").collectList().block().shouldNotBeNull()
        bois.`should not be empty`()
    }
}