package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    lateinit var database: RemindersDatabase

    private val reminder = ReminderDTO(
        "Mardin",
        "memory with my family",
        "Cag Urfa Sofrası",
        36.79,
        37.45,
        "123"
    )
    private val reminder2 = ReminderDTO(
        "Derbekir",
        "memory with my family",
        "Babababa",
        36.79,
        37.45,
        "456"
    )

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDB() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }


    @Test
    fun insertReminderAndGet() = runBlockingTest {
        val dao = database.reminderDao()

        dao.saveReminder(reminder)

        val loadedReminder = dao.getReminderById("123")

        assertThat(loadedReminder as ReminderDTO, notNullValue())
        assertThat(reminder.id, `is`(loadedReminder.id))
        assertThat(reminder.description, `is`(loadedReminder.description))
        assertThat(reminder.location, `is`(loadedReminder.location))
        assertThat(reminder.latitude, `is`(loadedReminder.latitude))
        assertThat(reminder.longitude, `is`(loadedReminder.longitude))
    }


    @Test
    fun deleteAllReminder_checkIsEmpty() = runBlockingTest {
        val dao = database.reminderDao()
        dao.saveReminder(reminder)
        dao.saveReminder(reminder2)

        dao.deleteAllReminders()

        val reminders = dao.getReminders()

        assertThat(reminders, `is`(emptyList()))
    }
    @After
    fun closeDB() {
        database.close()
    }
}