package br.com.rodroid.pantry_control.dto

import java.time.LocalDate

data class PantryRequest(val audioTranscript: String)

data class PantryResponse(val items: List<PantryItem>)

data class PantryItem(
    val name: String,
    val quantity: Double,
    val unit: String,
    val category: String,
    val expirationDate: LocalDate?,
    val originalText: String?
)