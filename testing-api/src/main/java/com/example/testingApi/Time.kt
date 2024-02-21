package com.example.testingApi

import java.time.LocalDate
import java.time.Period

class DateProvider(private val getDate: () -> LocalDate = { LocalDate.now() }) {
    operator fun invoke() = getDate()
}

fun calculateAge(now: LocalDate, birthDate: LocalDate): Int {
    return Period.between(birthDate, now).years
}