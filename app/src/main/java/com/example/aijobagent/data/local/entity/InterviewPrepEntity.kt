package com.example.aijobagent.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.aijobagent.domain.model.InterviewPrep
import com.example.aijobagent.domain.model.InterviewQuestion
import com.example.aijobagent.domain.model.QuestionCategory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "interview_preps")
data class InterviewPrepEntity(
    @PrimaryKey val id: String,
    val jobId: String,
    val questionsJson: String, // JSON serialized list
    val generatedAt: Long
)

fun InterviewPrepEntity.toDomain(): InterviewPrep {
    val questions = try {
        Json.decodeFromString<List<SerializableQuestion>>(questionsJson).map { it.toDomain() }
    } catch (_: Exception) { emptyList() }
    return InterviewPrep(id, jobId, questions, generatedAt)
}

fun InterviewPrep.toEntity(): InterviewPrepEntity {
    val serializable = questions.map { SerializableQuestion.fromDomain(it) }
    return InterviewPrepEntity(id, jobId, Json.encodeToString(serializable), generatedAt)
}

@kotlinx.serialization.Serializable
private data class SerializableQuestion(
    val question: String,
    val category: String,
    val suggestedAnswer: String
) {
    fun toDomain() = InterviewQuestion(
        question = question,
        category = try { QuestionCategory.valueOf(category) } catch (_: Exception) { QuestionCategory.TECHNICAL },
        suggestedAnswer = suggestedAnswer
    )
    companion object {
        fun fromDomain(q: InterviewQuestion) = SerializableQuestion(q.question, q.category.name, q.suggestedAnswer)
    }
}
