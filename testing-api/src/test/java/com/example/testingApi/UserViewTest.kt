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

class UserViewTest {
    private val mockHttp = MockHttp()
    private val userService = mockHttp.getUserService()
    private val userRepository = UserRepository(userService)
    private val sessionManager = SessionManager()
    private val dateProvider = DateProvider { LocalDate.parse("2024-02-21") }
    private val scope = TestScope()
    private val dispatcherProvider = DispatcherProvider(scope)

    private val testUser = User(
        id = "123123",
        name = "Marc",
        birthday = LocalDate.parse("1986-10-12")
    )

    @Test
    fun `Simple success case`() {
        mockHttp.mock("getUser") { testUser }
        sessionManager.logIn(testUser.id)

        scope.runTest {
            val sut = UserViewModel(
                userRepository,
                sessionManager,
                dateProvider,
                dispatcherProvider
            )

            runCurrent()

            assertEquals(
                UserViewModel.ViewUserData("Marc", "37 years"),
                sut.viewData
            )
        }
    }

    @Test
    fun `Is first loading and empty, then done loading and data is present`() {
        val processingTime = 2000L

        mockHttp.mock("getUser") {
            delay(processingTime)
            testUser
        }
        sessionManager.logIn(testUser.id)

        scope.runTest {
            val sut = UserViewModel(
                userRepository,
                sessionManager,
                dateProvider,
                dispatcherProvider
            )

            runCurrent()

            assertTrue(sut.isLoading)

            assertEquals(
                UserViewModel.ViewUserData.EMPTY,
                sut.viewData
            )

            advanceTimeBy(processingTime + 1)

            assertFalse(sut.isLoading)

            assertEquals(
                UserViewModel.ViewUserData("Marc", "37 years"),
                sut.viewData
            )
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `When user is not logged in, an exception is thrown`() {
        scope.runTest {
            UserViewModel(userRepository, sessionManager, dateProvider, dispatcherProvider)
        }
    }

    @Test
    fun `When user is not found, an error message is shown to the user`() {
        val errorMessage = "user not found!!!"

        mockHttp.mock("getUser") { throw Exception(errorMessage) }
        sessionManager.logIn(testUser.id)

        scope.runTest {
            val sut = UserViewModel(
                userRepository,
                sessionManager,
                dateProvider,
                dispatcherProvider
            )

            runCurrent()

            assertEquals(
                errorMessage,
                sut.errorMessage
            )
        }
    }
}