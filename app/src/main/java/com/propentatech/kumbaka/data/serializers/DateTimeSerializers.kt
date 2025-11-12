package com.propentatech.kumbaka.data.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Sérialiseur pour LocalDate
 */
object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor = 
        PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE))
    }
    
    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString(), DateTimeFormatter.ISO_LOCAL_DATE)
    }
}

/**
 * Sérialiseur pour LocalTime
 */
object LocalTimeSerializer : KSerializer<LocalTime> {
    override val descriptor: SerialDescriptor = 
        PrimitiveSerialDescriptor("LocalTime", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: LocalTime) {
        encoder.encodeString(value.format(DateTimeFormatter.ISO_LOCAL_TIME))
    }
    
    override fun deserialize(decoder: Decoder): LocalTime {
        return LocalTime.parse(decoder.decodeString(), DateTimeFormatter.ISO_LOCAL_TIME)
    }
}

/**
 * Sérialiseur pour LocalDateTime
 */
object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor = 
        PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
    }
    
    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
}

/**
 * Sérialiseur pour DayOfWeek
 */
object DayOfWeekSerializer : KSerializer<DayOfWeek> {
    override val descriptor: SerialDescriptor = 
        PrimitiveSerialDescriptor("DayOfWeek", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: DayOfWeek) {
        encoder.encodeString(value.name)
    }
    
    override fun deserialize(decoder: Decoder): DayOfWeek {
        return DayOfWeek.valueOf(decoder.decodeString())
    }
}

/**
 * Sérialiseur pour List<DayOfWeek>
 */
object DayOfWeekListSerializer : KSerializer<List<DayOfWeek>> {
    private val listSerializer = kotlinx.serialization.builtins.ListSerializer(DayOfWeekSerializer)
    
    override val descriptor: SerialDescriptor = listSerializer.descriptor
    
    override fun serialize(encoder: Encoder, value: List<DayOfWeek>) {
        listSerializer.serialize(encoder, value)
    }
    
    override fun deserialize(decoder: Decoder): List<DayOfWeek> {
        return listSerializer.deserialize(decoder)
    }
}
