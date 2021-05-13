package com.wency.petmanager.data

data class User(
    val id: String,
    val name: String,
    val email: String,
    val friendList: List<String>,
    val petList: List<String>
)
