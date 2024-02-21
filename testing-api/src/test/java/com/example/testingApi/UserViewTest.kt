@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.testingApi

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

private const val LOADING_TIME = 2000L
private const val NOT_FOUND_ERROR_MESSAGE = "user not found!!!"
private val TEST_USER = User(
    id = "123123",
    name = "Marc",
    birthday = LocalDate.parse("1986-10-12")
)

class UserViewTest {
    private val mockHttp = MockHttp()
    private val userService = mockHttp.getUserService()
    private val userRepository = UserRepository(userService)
    private val sessionManager = SessionManager()
    private val dateProvider = DateProvider { LocalDate.parse("2024-02-21") }
    private val scope = TestScope()
    private val dispatcherProvider = DispatcherProvider(scope)

    private lateinit var sut: UserViewModel

    @Test
    fun `Simple success case`() {
        givenBackendUser()
        givenLoggedIn()

        scope.runTest {
            whenInitializing()

            runCurrent()

            thenUserShown(
                name = "Marc",
                age = "37 years"
            )
        }
    }

    @Test
    fun `Is first loading and empty, then done loading and data is present`() {
        givenBackendUser(loadingTime = LOADING_TIME)
        givenLoggedIn()

        scope.runTest {
            whenInitializing()

            runCurrent()

            thenLoading()
            thenEmptyUserDataShown()

            advanceTimeBy(LOADING_TIME + 1)

            thenNotLoading()
            thenUserShown(
                name = "Marc",
                age = "37 years"
            )
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `When user is not logged in, an exception is thrown`() {
        scope.runTest {
            whenInitializing()
        }
    }

    @Test
    fun `When user is not found, an error message is shown to the user`() {
        givenBackendUser(failsWith = NOT_FOUND_ERROR_MESSAGE)
        givenLoggedIn()

        scope.runTest {
            whenInitializing()

            runCurrent()

            thenErrorMessageShown()
        }
    }

    private fun givenBackendUser(
        user: User = TEST_USER,
        loadingTime: Long = 0,
        failsWith: String? = null
    ) {
        mockHttp.mock("getUser") {
            delay(loadingTime)
            if (failsWith == null)
                user
            else
                throw Exception(failsWith)
        }
    }

    private fun givenLoggedIn() {
        sessionManager.logIn(TEST_USER.id)
    }

    private fun whenInitializing() {
        sut = UserViewModel(
            userRepository,
            sessionManager,
            dateProvider,
            dispatcherProvider
        )
    }

    private fun thenUserShown(name: String, age: String) =
        assertEquals(UserViewModel.ViewUserData(name, age), sut.state.value.viewData)

    private fun thenEmptyUserDataShown() =
        thenUserShown(
            name = "",
            age = ""
        )

    private fun thenErrorMessageShown() {
        assertEquals(NOT_FOUND_ERROR_MESSAGE, sut.state.value.errorMessage)
    }

    private fun thenLoading() = assertTrue(sut.state.value.isLoading)

    private fun thenNotLoading() = assertFalse(sut.state.value.isLoading)
}