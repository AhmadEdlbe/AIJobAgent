package com.example.aijobagent.data.local

import androidx.room.TypeConverter
import com.example.aijobagent.domain.model.JobSource
import com.example.aijobagent.domain.model.Seniority
import com.example.aijobagent.domain.model.TechStack
import com.example.aijobagent.domain.model.WorkMode

class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString("|") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split("|")
    }

    @TypeConverter
    fun fromTechStackList(value: List<TechStack>?): String {
        return value?.joinToString(",") { it.name } ?: ""
    }

    @TypeConverter
    fun toTechStackList(value: String): List<TechStack> {
        return if (value.isEmpty()) emptyList() else value.split(",").mapNotNull {
            try { TechStack.valueOf(it) } catch (_: Exception) { null }
        }
    }

    @TypeConverter
    fun fromWorkMode(mode: WorkMode): String = mode.name

    @TypeConverter
    fun toWorkMode(value: String): WorkMode = try { WorkMode.valueOf(value) } catch (_: Exception) { WorkMode.REMOTE }

    @TypeConverter
    fun fromSeniority(seniority: Seniority): String = seniority.name

    @TypeConverter
    fun toSeniority(value: String): Seniority = try { Seniority.valueOf(value) } catch (_: Exception) { Seniority.MID_LEVEL }

    @TypeConverter
    fun fromJobSource(source: JobSource): String = source.name

    @TypeConverter
    fun toJobSource(value: String): JobSource = try { JobSource.valueOf(value) } catch (_: Exception) { JobSource.MANUAL }
}
