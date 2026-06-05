package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    @Json(name = "temperature") val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class CandidatePart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class CandidateContent(
    @Json(name = "parts") val parts: List<CandidatePart>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: CandidateContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class PlayerUpdateInfo(
    @Json(name = "name") val name: String,
    @Json(name = "clubAndCountry") val clubAndCountry: String,
    @Json(name = "photoUrl") val photoUrl: String? = null
)

object GeminiApiClient {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun fetchPlayerUpdates(playerName: String): PlayerUpdateInfo? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // High-fidelity simulation fallback for local development:
            val lower = playerName.lowercase()
            val simulated = when {
                lower.contains("pelé") -> PlayerUpdateInfo(
                    name = "Pelé Éternel 👑",
                    clubAndCountry = "Santos / Brasil",
                    photoUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150"
                )
                lower.contains("ronaldinho") -> PlayerUpdateInfo(
                    name = "Ronaldinho Gaúcho 🤙",
                    clubAndCountry = "Barcelona / Brasil",
                    photoUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150"
                )
                lower.contains("vini") -> PlayerUpdateInfo(
                    name = "Vini Jr ⚡",
                    clubAndCountry = "Real Madrid / Brasil",
                    photoUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150"
                )
                lower.contains("neymar") -> PlayerUpdateInfo(
                    name = "Neymar Jr 🇧🇷",
                    clubAndCountry = "Al-Hilal / Brasil",
                    photoUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150"
                )
                lower.contains("kaká") -> PlayerUpdateInfo(
                    name = "Kaká Magnífico 🔴⚫",
                    clubAndCountry = "Milan / Brasil",
                    photoUrl = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=150"
                )
                lower.contains("estêvão") -> PlayerUpdateInfo(
                    name = "Estêvão (Miojo) 🍝",
                    clubAndCountry = "Chelsea / Brasil",
                    photoUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=150"
                )
                lower.contains("endrick") -> PlayerUpdateInfo(
                    name = "Endrick Bobby ⚡",
                    clubAndCountry = "Real Madrid / Brasil",
                    photoUrl = "https://images.unsplash.com/photo-1518063319789-7217e6706b04?w=150"
                )
                else -> PlayerUpdateInfo(
                    name = "$playerName (IA)",
                    clubAndCountry = "Club Mundial / Brasil",
                    photoUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=150"
                )
            }
            return@withContext simulated
        }

        val prompt = """
            Você é um assistente de dados esportivos especialista em futebol e estatísticas da FIFA.
            Dado o jogador '$playerName', identifique as seguintes informações ATUALIZADAS e REAIS de 2026:
            1. Nome atualizado e oficial curto do jogador (ex: "Cristiano Ronaldo", "Neymar Jr", "Lionel Messi", "K. Mbappé").
            2. Equipe/Clube atual no formato 'Clube / Seleção' (ex: "Al-Nassr / Portugal", "Inter Miami / Argentina", "Al-Hilal / Brasil", "Real Madrid / França").
            3. Selecione uma URL de imagem de preset ou uma imagem pública esportiva do Unsplash para o jogador. Retorne um dos links abaixo que melhor se adapte ou use uma imagem Unsplash similar:
               - https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150 (Golden Portrait)
               - https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150 (Neon Blue Portrait)
               - https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150 (Sleek Dark Portrait)
               - https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=150 (Stadium Turf)
               - https://images.unsplash.com/photo-1518063319789-7217e6706b04?w=150 (Retro Match Background)

            Você DEVE retornar estritamente um objeto JSON com as seguintes chaves correspondentes:
            {
              "name": "...",
              "clubAndCountry": "...",
              "photoUrl": "..."
            }
        """.trimIndent()

        val requestObj = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = prompt))
                )
            ),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2f
            )
        )

        val requestAdapter = moshi.adapter(GeminiRequest::class.java)
        val requestJson = requestAdapter.toJson(requestObj)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toRequestBody(mediaType)

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext null
                }
                val bodyString = response.body?.string() ?: return@withContext null
                val responseAdapter = moshi.adapter(GeminiResponse::class.java)
                val geminiRes = responseAdapter.fromJson(bodyString)
                val responseText = geminiRes?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: return@withContext null

                val updateAdapter = moshi.adapter(PlayerUpdateInfo::class.java)
                return@withContext updateAdapter.fromJson(responseText)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}
