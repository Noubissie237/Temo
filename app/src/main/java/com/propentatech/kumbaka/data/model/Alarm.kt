package com.propentatech.kumbaka.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val time: String, // Format HH:mm
    val label: String = "",
    val isEnabled: Boolean = true,
    val vibrate: Boolean = true,
    val soundUri: String? = null,
    val soundName: String = "Par défaut",
    val daysOfWeek: String = "1111111" // LMTJVSD (1 = actif)
)
