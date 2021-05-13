package com.wency.petmanager.data

import android.graphics.Bitmap
import java.util.*

data class Pet(
    val id: String,
    val name: String,
    val photo: String,
    val birth: Date?,
    val users: List<String>,
    val tags: List<String>,
    val events: List<String>
)
