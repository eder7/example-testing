package com.example.testingApi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val dateProvider: DateProvider,
    dispatcherProvider: DispatcherProvider
) {
    private val scope = CoroutineScope(dispatcherProvider.main)

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        scope.launch {
            loadData()
        }
    }

    private suspend fun loadData() {
        val userId = sessionManager.currentUserId ?: error("no current user")
        try {
            resetError()
            setIsLoading(true)
            userRepository.getUser(userId)
                .let(::applyUser)
        } catch (exception: Exception) {
            setError(exception.message)
        } finally {
            setIsLoading(false)
        }
    }

    private fun applyUser(value: User) {
        _state.update {
            it.copy(
                viewData = ViewUserData(
                    name = value.name,
                    age = "${calculateAge(dateProvider(), value.birthday)} years"
                )
            )
        }
    }

    private fun setIsLoading(value: Boolean) {
        _state.update {
            it.copy(
                isLoading = value
            )
        }
    }

    private fun setError(value: String?) {
        _state.update {
            it.copy(
                errorMessage = value
            )
        }
    }

    private fun resetError() = setError(null)

    data class ViewUserData(
        val name: String,
        val age: String
    ) {
        companion object {
            val EMPTY = ViewUserData(
                name = "",
                age = ""
            )
        }
    }

    data class State(
        var viewData: ViewUserData = ViewUserData.EMPTY,
        var errorMessage: String? = null,
        var isLoading: Boolean = false,
    )
}
