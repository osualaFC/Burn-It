package com.example.burn_it.ui

import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.filters.MediumTest
import com.example.burn_it.R
import com.example.burn_it.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify

@MediumTest
@HiltAndroidTest
@ExperimentalCoroutinesApi
class RunFragmentTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp(){
        hiltRule.inject()
    }

    @Test
    fun navigateToTargetFragment(){
        val navController = Mockito.mock(NavController::class.java)

        launchFragmentInHiltContainer<RunFragment> {
            Navigation.setViewNavController(requireView(), navController)
        }
        onView(ViewMatchers.withId(R.id.fab_target)).perform(ViewActions.click())

        verify(navController).navigate(R.id.targetFragment)
    }
}