package br.com.rodroid.pantry_control.service

import br.com.rodroid.pantry_control.dto.PantryResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import tools.jackson.databind.ObjectMapper
import java.time.LocalDate

@Service
class GeminiService(
    @Value("\${gemini.api.key}") private val apiKey: String,
    private val objectMapper: ObjectMapper
) {
    private val restClient = RestClient.create("https://generativelanguage.googleapis.com/v1beta/models")

    fun processPantryItems(transcript: String): PantryResponse {
        val currentDate = LocalDate.now().toString()
        
        val finalPrompt = """
            Hoje é: $currentDate.
            Analise este texto: "$transcript"
            Retorne JSON seguindo o schema definido.
        """.trimIndent()

        val requestBody = mapOf(
            "contents" to listOf(
                mapOf("parts" to listOf(mapOf("text" to finalPrompt)))
            ),
            "generationConfig" to mapOf(
                "response_mime_type" to "application/json"
            ),
        )

        val response = restClient.post()
            .uri("/gemini-2.5-flash:generateContent?key=$apiKey")
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
            .retrieve()
            .body(String::class.java)

        return parseGeminiResponse(response) 
    }

    private fun parseGeminiResponse(jsonResponse: String?): PantryResponse {
        val rootNode = objectMapper.readTree(jsonResponse)
        val jsonText = rootNode["candidates"][0]["content"]["parts"][0]["text"].asText()
        
        return objectMapper.readValue(jsonText, PantryResponse::class.java)
    }
}