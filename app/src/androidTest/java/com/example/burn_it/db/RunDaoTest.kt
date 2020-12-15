package com.example.burn_it.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.burn_it.getOrAwaitValue
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RunDaoTest {
    
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var database: RunDatabase
    private lateinit var dao: RunDAO

    @Before
    fun setUp(){
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), RunDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = database.getRunDao()
    }

    @After
    fun tearDown(){
        database.close()
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

        val allRuns = dao.getAllRunsSortedByAvgSpeed().getOrAwaitValue()

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

        val allRuns = dao.getAllRunsSortedByAvgSpeed().getOrAwaitValue()

        assertThat(allRuns).isEmpty()

    }
    
    
}