package com.example.testingApi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class UserRepository(private val userService: UserService) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private val cache = mutableMapOf<String, User>()

    suspend fun getUser(id: String) =
        cache.getOrPut(id) { userService.getUser(id) }

    fun putUser(value: User) {
        cache[value.id] = value
        scope.launch {
            userService.putUser(value)
        }
    }
}

data class User(
    val id: String,
    val name: String,
    val birthday: LocalDate
)