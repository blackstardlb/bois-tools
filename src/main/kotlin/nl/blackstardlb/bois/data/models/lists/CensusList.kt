package nl.blackstardlb.bois.data.models.lists

interface CensusList<T> {
    val data: List<T>
    val returned: Int
}