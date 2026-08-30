package com.example.aijobagent.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.aijobagent.domain.model.CoverLetter

@Entity(tableName = "cover_letters")
data class CoverLetterEntity(
    @PrimaryKey val id: String,
    val jobId: String,
    val content: String,
    val generatedAt: Long,
    val isEdited: Boolean
)

fun CoverLetterEntity.toDomain() = CoverLetter(id, jobId, content, generatedAt, isEdited)
fun CoverLetter.toEntity() = CoverLetterEntity(id, jobId, content, generatedAt, isEdited)
