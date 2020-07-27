package nl.blackstardlb.bois.data.repositories

import mu.KotlinLogging
import nl.blackstardlb.bois.runTesting
import org.amshove.kluent.*
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
    fun getOutfitForTag() = runTesting {
        val bois = outfitCensusRepository.getOutfitForTag("BOIS").shouldNotBeNull()
        bois.alias `should be equal to` "BOIS"
    }

    @Test
    fun getMembersForTag() = runTesting {
        val bois = outfitCensusRepository.getOutfitForTag("BOIS").shouldNotBeNull()
        val boisMembers = outfitCensusRepository.getFullOutfitMembersForTag("BOIS").shouldNotBeNull()
        boisMembers.`should not be empty`()
        boisMembers.size.shouldBeLessOrEqualTo(bois.memberCount.toInt())
    }
}