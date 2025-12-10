package org.example

import java.time.LocalDate

data class Tweet(
    val rawDate: String,
    val date: LocalDate?,
    val text: String,
    val country: String
)
