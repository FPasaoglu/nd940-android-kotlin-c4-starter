package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val reminder = ReminderDTO(
        "Mardin",
        "memory with my family",
        "Cag Urfa Sofrası",
        36.79,
        37.45,
        "12345"
    )

    @Before
    fun initDB() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDB() {
        database.close()
    }

    @Test
    fun saveReminder_checkEqualToRepository() = runBlocking {
        repository.saveReminder(reminder)

        val remindersListFromRepo = (repository.getReminders() as Result.Success).data

        assertThat(remindersListFromRepo[0].id, `is`(reminder.id))
        assertThat(remindersListFromRepo[0].title, `is`(reminder.title))
        assertThat(remindersListFromRepo[0].description, `is`(reminder.description))
        assertThat(remindersListFromRepo[0].latitude, `is`(reminder.latitude))
        assertThat(remindersListFromRepo[0].longitude, `is`(reminder.longitude))
        assertThat(remindersListFromRepo[0].location, `is`(reminder.location))

    }

    @Test
    fun deleteAllReminders_checkIsEmpty() = runBlocking {

        repository.saveReminder(reminder)

        var remindersListFromRepo = (repository.getReminders() as Result.Success).data

        repository.deleteAllReminders()

        remindersListFromRepo = (repository.getReminders() as Result.Success).data

        assertThat(remindersListFromRepo, `is`(emptyList()))
    }
}