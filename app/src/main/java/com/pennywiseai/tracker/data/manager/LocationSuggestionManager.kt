package com.pennywiseai.tracker.data.manager

import com.pennywiseai.tracker.data.repository.TransactionRepository
import javax.inject.Inject
import javax.inject.Singleton

data class LocationExpenseSuggestion(
    val suggestedCategory: String,
    val suggestedMerchant: String,
    val frequencyCount: Int,
    val averageAmount: java.math.BigDecimal?
)

/**
 * Manages location-based expense suggestions using device geolocation and historical transaction proximity.
 */
@Singleton
class LocationSuggestionManager @Inject constructor(
    private val locationManagerHelper: LocationManagerHelper,
    private val transactionRepository: TransactionRepository
) {
    /**
     * Gets expense suggestions based on the device's current location by finding nearby historical transactions.
     */
    suspend fun getSuggestionsForCurrentLocation(): List<LocationExpenseSuggestion> {
        val location = locationManagerHelper.getCurrentLocation() ?: return emptyList()
        val nearbyTransactions = transactionRepository.getTransactionsNearLocation(location.first, location.second)
        if (nearbyTransactions.isEmpty()) return emptyList()

        // Group by merchant to provide smart suggestions
        return nearbyTransactions
            .groupBy { it.merchantName }
            .map { (merchant, txns) ->
                val mostFrequentCategory = txns.groupBy { it.category }
                    .maxByOrNull { it.value.size }?.key ?: "Uncategorized"
                val avgAmount = if (txns.isNotEmpty()) {
                    txns.map { it.amount }.reduce { acc, dec -> acc.add(dec) }
                        .divide(java.math.BigDecimal(txns.size), 2, java.math.RoundingMode.HALF_UP)
                } else null

                LocationExpenseSuggestion(
                    suggestedCategory = mostFrequentCategory,
                    suggestedMerchant = merchant,
                    frequencyCount = txns.size,
                    averageAmount = avgAmount
                )
            }
            .sortedByDescending { it.frequencyCount }
    }
}
