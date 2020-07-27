package nl.blackstardlb.bois.data.models

data class CensusJoin(val target: String,
                      val on: String,
                      val to: String,
                      val injectAt: String? = null,
                      val isList: Boolean = false,
                      val terms: List<String> = emptyList(),
                      val show: List<String> = emptyList(),
                      val hide: List<String> = emptyList(),
                      val includeNonMatches: Boolean = true
) {
    fun build(): String {
        val terms = if (terms.isEmpty()) "" else "^terms:${terms.joinToString("'")}"
        val list = if (isList) "^list:1" else ""
        val injectAt = injectAt?.let { "^inject_at:$injectAt" } ?: ""
        val show = if (show.isEmpty()) "" else "^show:${show.joinToString("'")}"
        val hide = if (hide.isEmpty()) "" else "^hide:${hide.joinToString("'")}"
        val outer = if (!includeNonMatches) "^outer:0" else ""
        return "$target^on:$on^to:$to$injectAt$list$terms$show$hide$outer"
    }
}