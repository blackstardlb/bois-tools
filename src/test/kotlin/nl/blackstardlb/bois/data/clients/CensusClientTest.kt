package nl.blackstardlb.bois.data.clients

import mu.KotlinLogging
import nl.blackstardlb.bois.data.models.lists.OutfitList
import nl.blackstardlb.bois.runTesting
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

private val logger = KotlinLogging.logger {}

@SpringBootTest
@ActiveProfiles("test")
internal class CensusClientTest {
    @Autowired
    lateinit var censusClient: CensusClient

    @Test
    fun canSendMessage() = runTesting {
        val outfitList = censusClient.sendRequestWithRetry<OutfitList>("outfit", uriParameters = listOf("alias" to listOf("BOIS")))
        logger.info { outfitList }
    }
}