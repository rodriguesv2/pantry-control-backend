package br.com.rodroid.pantry_control.service

import br.com.rodroid.pantry_control.dto.PantryResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile
import tools.jackson.databind.ObjectMapper
import java.time.LocalDate
import java.util.Base64
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
        return PantryResponse(emptyList())
    }

    fun processAudio(file: MultipartFile): PantryResponse {
        val currentDate = LocalDate.now().toString()
        val finalPrompt = promptTemplate.replace("{{CURRENT_DATE}}", currentDate)

        val audioBase64 = Base64.getEncoder().encodeToString(file.bytes)
        val mimeType = file.contentType ?: "audio/mp3"

        val requestBody = mapOf(
            "contents" to listOf(
                mapOf(
                    "parts" to listOf(
                        mapOf("text" to finalPrompt),
                        mapOf(
                            "inline_data" to mapOf(
                                "mime_type" to mimeType,
                                "data" to audioBase64
                            )
                        )
                    )
                )
            ),
            "generationConfig" to mapOf("response_mime_type" to "application/json")
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
        val candidate = rootNode["candidates"]?.get(0)
            ?: throw RuntimeException("Gemini não retornou candidatos. Verifique Safety Settings.")

        val jsonText = candidate["content"]["parts"][0]["text"].asText()
        return objectMapper.readValue(jsonText, PantryResponse::class.java)
    }
}