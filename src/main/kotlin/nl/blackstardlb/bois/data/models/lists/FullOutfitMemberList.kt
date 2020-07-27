package nl.blackstardlb.bois.data.models.lists

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import nl.blackstardlb.bois.data.models.FullOutfitMember

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class FullOutfitMemberList(@JsonProperty("outfit_member_list") override val data: List<FullOutfitMember>, override val returned: Int) : CensusList<FullOutfitMember>