package com.example.burn_it.ui

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
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
class SetUpFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp(){
        hiltRule.inject()
    }

    @Test
    fun inputDetails_NavigateToRunFragment(){
        val navController = Mockito.mock(NavController::class.java)
        val bundle = Mockito.mock(Bundle::class.java)
        val options = Mockito.mock(NavOptions::class.java)

        launchFragmentInHiltContainer<SetupFragment> {
           Navigation.setViewNavController(requireView(), navController)
        }
        onView(ViewMatchers.withId(R.id.etName)).perform(ViewActions.replaceText("fred"))

        onView(ViewMatchers.withId(R.id.etWeight)).perform(ViewActions.replaceText("70"))

        onView(ViewMatchers.withId(R.id.tvContinue)).perform(ViewActions.click())

        verify(navController).navigate(R.id.action_setupFragment_to_runFragment, bundle, options)

    }
}