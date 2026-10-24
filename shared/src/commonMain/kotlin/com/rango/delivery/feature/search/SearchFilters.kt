package com.rango.delivery.feature.search

import com.rango.delivery.core.format.formatPrice
import com.rango.delivery.data.search.ProductQuery

enum class SearchSort(val apiValue: String, val optionLabel: String, val shortLabel: String) {
    Relevance("relevance", "Relevância", "Relevância"),
    PriceAsc("price_asc", "Preço · crescente", "Menor preço"),
    PriceDesc("price_desc", "Preço · decrescente", "Maior preço"),
    Rating("rating", "Melhor avaliados", "Melhor avaliados"),
}

data class SearchCategory(val slug: String, val name: String)

enum class FilterKey { Category, Price, Rating, Time, Sort }

data class ActiveFilter(val key: FilterKey, val label: String)

data class SearchFilters(
    val category: SearchCategory? = null,
    val maxPrice: Int = PRICE_MAX,
    val minRating: Double = 0.0,
    val maxTime: Int = TIME_ANY,
    val sort: SearchSort = SearchSort.Relevance,
) {
    val active: List<ActiveFilter>
        get() = buildList {
            category?.let { add(ActiveFilter(FilterKey.Category, it.name)) }
            if (maxPrice != PRICE_MAX) add(ActiveFilter(FilterKey.Price, "até ${formatPrice(maxPrice.toDouble())}"))
            if (minRating > 0) add(ActiveFilter(FilterKey.Rating, "${ratingLabel(minRating)}+ estrelas"))
            if (maxTime != TIME_ANY) add(ActiveFilter(FilterKey.Time, "até $maxTime min"))
            if (sort != SearchSort.Relevance) add(ActiveFilter(FilterKey.Sort, sort.shortLabel))
        }

    fun without(key: FilterKey): SearchFilters = when (key) {
        FilterKey.Category -> copy(category = null)
        FilterKey.Price -> copy(maxPrice = PRICE_MAX)
        FilterKey.Rating -> copy(minRating = 0.0)
        FilterKey.Time -> copy(maxTime = TIME_ANY)
        FilterKey.Sort -> copy(sort = SearchSort.Relevance)
    }

    fun toQuery(term: String) = ProductQuery(
        term = term.trim().ifEmpty { null },
        categorySlug = category?.slug,
        maxPrice = maxPrice.takeIf { it != PRICE_MAX },
        minRating = minRating.takeIf { it > 0 },
        maxDeliveryTime = maxTime.takeIf { it != TIME_ANY },
        sort = sort.apiValue,
    )

    companion object {
        const val PRICE_MIN = 10
        const val PRICE_MAX = 80
        const val PRICE_STEP = 5
        const val TIME_ANY = 60

        val ratingOptions = listOf(0.0 to "Todas", 4.0 to "4,0+", 4.5 to "4,5+", 4.8 to "4,8+")
        val timeOptions = listOf(TIME_ANY to "Qualquer", 30 to "até 30 min", 25 to "até 25 min")

        fun forHomeSection(key: String?): SearchFilters = when (key) {
            "offers" -> SearchFilters(sort = SearchSort.PriceAsc)
            "popular" -> SearchFilters(sort = SearchSort.Rating)
            "recommended" -> SearchFilters(minRating = 4.5, sort = SearchSort.Rating)
            "fast" -> SearchFilters(maxTime = 30)
            else -> SearchFilters()
        }

        private fun ratingLabel(value: Double) = value.toString().replace('.', ',')
    }
}
