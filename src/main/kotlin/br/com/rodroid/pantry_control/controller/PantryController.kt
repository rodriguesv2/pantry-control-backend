package br.com.rodroid.pantry_control.controller

import br.com.rodroid.pantry_control.dto.PantryRequest
import br.com.rodroid.pantry_control.dto.PantryResponse
import br.com.rodroid.pantry_control.service.GeminiService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/pantry")
class PantryController(
    private val geminiService: GeminiService
) {

    @PostMapping("/analyze")
    fun analyzeTranscript(@RequestBody request: PantryRequest): ResponseEntity<PantryResponse> {
        if (request.audioTranscript.isBlank()) {
            return ResponseEntity.badRequest().build()
        }

        val result = geminiService.processPantryItems(request.audioTranscript)
        return ResponseEntity.ok(result)
    }
}