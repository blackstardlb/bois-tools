package nl.blackstardlb.bois.config

import com.github.kittinunf.fuel.core.FuelManager
import nl.blackstardlb.bois.data.clients.CensusClientFuelImpl
import org.apache.http.impl.client.HttpClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class CensusConfig(
        @Value("\${census.api_url}") val censusApiURL: String,
        @Value("\${webclient.max_memory_in_mb}") val maxWebClientMemoryInMb: Int
) {

    @Bean
    fun fuelManager(): FuelManager {
        val instance = FuelManager.instance
        instance.basePath = censusApiURL
        instance.addResponseInterceptor(CensusClientFuelImpl.responseLogger)
        instance.addRequestInterceptor(CensusClientFuelImpl.requestLogger)
        return instance
    }

    @Bean
    fun httpClient(): org.apache.http.client.HttpClient {
        return HttpClients.createDefault()
    }
}