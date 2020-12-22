package com.example.burn_it.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.burn_it.getOrAwaitValue
import com.example.burn_it.launchFragmentInHiltContainer
import com.example.burn_it.ui.*
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
@HiltAndroidTest
@SmallTest
class RunDaoTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named("test_db")
     lateinit var database: RunDatabase
     lateinit var dao: RunDAO

    @Before
    fun setUp(){
        hiltRule.inject()
        dao = database.getRunDao()
    }

    @After
    fun tearDown(){
        database.close()
    }

    @Test
    fun testLaunchFragmentHiltContainer(){
        launchFragmentInHiltContainer<RunFragment> {  }
    }


    @Test
    fun insertRun() = runBlockingTest {
        val run = Run("image", 0L, 0f, 0,0L, 0, id = 1)
        dao.insertRun(run)

        val allRuns = dao.getAllRunsSortedByAvgSpeed().getOrAwaitValue()

        assertThat(allRuns).contains(run)
    }

    @Test
    fun deleteRun() = runBlockingTest {
        val run = Run("image", 0L, 0f, 0,0L, 0)

        dao.insertRun(run)

        dao.deleteRun(run)

        val allRuns = dao.getAllRunsSortedByDate().getOrAwaitValue()

        assertThat(allRuns).doesNotContain(run)

    }

    @Test
    fun getTotalDistance() = runBlockingTest {
        val run =  Run("image", 0L, 0f, 2,0L, 0)
        val run3 = Run("image3", 0L, 0f, 2,0L, 0)
        val run4 = Run("image4", 0L, 0f, 2,0L, 0)

        dao.insertRun(run)
        dao.insertRun(run3)
        dao.insertRun(run4)

        val totalDist = dao.getTotalDistance().getOrAwaitValue()

        assertThat(totalDist).isEqualTo(6)
    }

    @Test
    fun deleteAll() = runBlockingTest {
        val run =  Run("image", 0L, 0f, 2,0L, 0)
        val run3 = Run("image3", 0L, 0f, 2,0L, 0)
        val run4 = Run("image4", 0L, 0f, 2,0L, 0)

        dao.insertRun(run)
        dao.insertRun(run3)
        dao.insertRun(run4)

       dao.deleteAllRuns()

        val allRuns = dao.getAllRunsSortedByDate().getOrAwaitValue()

        assertThat(allRuns).isEmpty()

    }
    
    
}