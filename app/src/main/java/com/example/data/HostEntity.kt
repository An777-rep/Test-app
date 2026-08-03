package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_hosts")
data class HostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val ipAddress: String,
    val macAddress: String = "",
    val port: Int = 8080,
    val wolPort: Int = 9,
    val isFavorite: Boolean = false,
    val lastSeenTimestamp: Long = System.currentTimeMillis()
)
