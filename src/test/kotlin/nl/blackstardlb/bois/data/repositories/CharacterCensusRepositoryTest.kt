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
internal class CharacterCensusRepositoryTest {
    @Autowired
    lateinit var characterCensusRepository: CharacterCensusRepository

    @Test
    fun getCharacterStatHistories() = runTesting {
        val blackStarDLBStats = characterCensusRepository.getCharacterStatHistories("5428072203476045489").shouldNotBeNull()
        blackStarDLBStats.`should not be empty`()
        blackStarDLBStats.map { it.id } shouldContainAll listOf("battle_rank", "certs", "deaths", "facility_capture", "facility_defend", "kills", "medals", "ribbons", "score", "time")
    }

    @Test
    fun getCharactersById() = runTesting {
        val characters = characterCensusRepository.getCharactersByIds(listOf("5428072203476045489")).`should not be null`()
        characters shouldHaveSize 1
        characters[0].name.first `should be equal to` "BlackStarDLB"
    }
}