package com.example.testingApi

class SessionManager {
    var currentUserId: String? = null

    fun logIn(userId: String) {
        currentUserId = userId
    }

    fun logOut() {
        currentUserId = null
    }
}