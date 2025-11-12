package com.propentatech.kumbaka.data.database

import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Convertisseurs de types pour Room Database
 * Permet de stocker des types complexes dans la base de données
 */
class Converters {
    
    // LocalDateTime
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.toString()
    }
    
    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }
    
    // LocalDate
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.toString()
    }
    
    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }
    
    // LocalTime
    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? {
        return value?.toString()
    }
    
    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it) }
    }
    
    // List<DayOfWeek>
    @TypeConverter
    fun fromDayOfWeekList(value: List<DayOfWeek>?): String? {
        return value?.joinToString(",") { it.name }
    }
    
    @TypeConverter
    fun toDayOfWeekList(value: String?): List<DayOfWeek>? {
        return value?.split(",")?.mapNotNull { 
            try {
                DayOfWeek.valueOf(it)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    // List<String>
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString("|||")
    }
    
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.split("|||")?.filter { it.isNotEmpty() }
    }
}
