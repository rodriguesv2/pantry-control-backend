package br.com.rodroid.pantry_control.service

import br.com.rodroid.pantry_control.dto.PantryResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.core.io.Resource
import tools.jackson.databind.ObjectMapper
import java.time.LocalDate
import kotlin.getValue

@Service
class GeminiService(
    @Value("\${gemini.api.key}") private val apiKey: String,
    @Value("classpath:prompts/pantry_parser.txt") private val promptResource: Resource,
    private val objectMapper: ObjectMapper
) {
    private val restClient = RestClient.create("https://generativelanguage.googleapis.com/v1beta/models")
    private val promptTemplate: String by lazy {
        promptResource.inputStream.bufferedReader().use { it.readText() }
    }

    fun processPantryItems(transcript: String): PantryResponse {
        val currentDate = LocalDate.now().toString()

        val finalPrompt = promptTemplate.replace("{{CURRENT_DATE}}", currentDate)

        val userContent = """
            $finalPrompt
            
            Input: "$transcript"
            Output:
        """.trimIndent()

        val requestBody = mapOf(
            "contents" to listOf(
                mapOf("parts" to listOf(mapOf("text" to userContent)))
            ),
            "generationConfig" to mapOf(
                "response_mime_type" to "application/json"
            ),
        )

        println("*********************************************************************************************")
        println(requestBody)
        println("*********************************************************************************************")

        val response = restClient.post()
            .uri("/gemini-2.5-flash:generateContent?key=$apiKey")
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
            .retrieve()
            .body(String::class.java)

        println("##############################################################################################")
        println(response)
        println("##############################################################################################")

        return parseGeminiResponse(response) 
    }

    private fun parseGeminiResponse(jsonResponse: String?): PantryResponse {
        val rootNode = objectMapper.readTree(jsonResponse)
        val jsonText = rootNode["candidates"][0]["content"]["parts"][0]["text"].asText()
        
        return objectMapper.readValue(jsonText, PantryResponse::class.java)
    }
}