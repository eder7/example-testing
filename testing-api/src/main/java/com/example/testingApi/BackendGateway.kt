package com.example.testingApi

interface UserService {
    suspend fun getUser(id: String): User
    suspend fun putUser(value: User)
}

class RealHttp : Http() {
    override fun getUserService(): UserService = TODO()
}

class MockHttp : Http() {

    private val methodToResultMap = mutableMapOf<String, suspend () -> Any>()

    override fun getUserService(): UserService = object : UserService {

        override suspend fun getUser(id: String): User {
            return executeMockBehavior("getUser") as User
        }

        override suspend fun putUser(value: User) {
            TODO()
        }
    }

    private suspend fun executeMockBehavior(methodName: String): Any {
        val mockBlock = methodToResultMap[methodName] ?: error("not mocked! please use mock()...")
        return mockBlock()
    }

    fun mock(method: String, body: suspend () -> Any) {
        methodToResultMap[method] = body
    }
}

abstract class Http {
    abstract fun getUserService(): UserService
}
