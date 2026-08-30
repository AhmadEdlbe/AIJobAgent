package com.example.aijobagent.data.remote.api

import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAiApiService {
    @POST("chat/completions")
    suspend fun chatCompletions(
        @Header("Authorization") auth: String,
        @Body body: ChatRequest
    ): ChatResponse
}

@JsonClass(generateAdapter = true)
data class ChatRequest(
    val model: String = "gpt-4o-mini",
    val messages: List<Message>,
    val temperature: Double = 0.7
) {
    @JsonClass(generateAdapter = true)
    data class Message(val role: String, val content: String)
}

@JsonClass(generateAdapter = true)
data class ChatResponse(
    val choices: List<Choice>
) {
    @JsonClass(generateAdapter = true)
    data class Choice(val message: Message)
    @JsonClass(generateAdapter = true)
    data class Message(val content: String)
}
