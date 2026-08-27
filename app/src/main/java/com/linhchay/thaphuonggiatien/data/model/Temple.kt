package com.linhchay.thaphuonggiatien.data.model

import java.io.Serializable

data class Temple(
    val id: Int,
    val name: String,
    val location: String,
    val imageRes: Int,
    val altarImageRes: Int
) : Serializable
