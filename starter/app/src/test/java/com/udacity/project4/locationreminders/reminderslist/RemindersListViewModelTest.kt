package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        stopKoin()
        val remindersList = mutableListOf<ReminderDTO>(
            ReminderDTO("Mardin", "memory with my family", "Cag Urfa Sofrası", 36.79, 37.45),
            ReminderDTO("Derbekir", "memory with my family", "Cag Urfa Sofrası", 36.79, 37.45),
            ReminderDTO("Bursa", "memory with my family", "Cag Urfa Sofrası", 36.79, 37.45),
            ReminderDTO("Izmit", "memory with my family", "Cag Urfa Sofrası", 36.79, 37.45)
        )

        fakeDataSource = FakeDataSource(remindersList)
        remindersListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @Test
    fun loadReminder_checkNotNull() {
        // GIVEN
        remindersListViewModel.loadReminders()

        // WHEN
        val viewModelList = remindersListViewModel.remindersList.getOrAwaitValue()

        // THEN
        assertThat(viewModelList, not(nullValue()))
    }

    @Test
    fun loadReminder_compareViewmodelWithDatasourceReminders() = mainCoroutineRule.runBlockingTest {

        remindersListViewModel.loadReminders()
        val reminderFromViewModel = remindersListViewModel.remindersList.getOrAwaitValue()
        val reminderFromDataSource = (fakeDataSource.getReminders() as Result.Success).data

        assertThat(reminderFromViewModel[0].id, `is`(reminderFromDataSource[0].id))
        assertThat(reminderFromViewModel[0].title, `is`(reminderFromDataSource[0].title))
        assertThat(reminderFromViewModel[0].description, `is`(reminderFromDataSource[0].description))
        assertThat(reminderFromViewModel[0].location, `is`(reminderFromDataSource[0].location))
        assertThat(reminderFromViewModel[0].longitude, `is`(reminderFromDataSource[0].longitude))
        assertThat(reminderFromViewModel[0].latitude, `is`(reminderFromDataSource[0].latitude))

    }

    @Test
    fun loadReminder_loading() {
        mainCoroutineRule.pauseDispatcher()

        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun loadReminder_whenUnavaibleOrEmpty() {
        fakeDataSource.setShouldReturnError(true)

        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.showErrorMessage.getOrAwaitValue(), `is`("Reminder Exception"))
    }
}