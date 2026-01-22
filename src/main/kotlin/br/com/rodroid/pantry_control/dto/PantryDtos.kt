package br.com.rodroid.pantry_control.dto

import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonNaming
import java.time.LocalDate

data class PantryRequest(val audioTranscript: String)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PantryResponse(val items: List<PantryItem>)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PantryItem(
    val name: String,
    val quantity: Double,
    val unit: String,
    val category: String,
    val expirationDate: LocalDate?,
    val originalText: String?
)