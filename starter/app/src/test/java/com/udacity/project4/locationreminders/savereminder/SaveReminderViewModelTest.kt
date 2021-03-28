package com.udacity.project4.locationreminders.savereminder

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

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
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)

    }

    @Test
    fun onClear_isNull() {
        saveReminderViewModel.onClear()

        assertThat(saveReminderViewModel.reminderTitle.getOrAwaitValue(), nullValue())
        assertThat(saveReminderViewModel.reminderDescription.getOrAwaitValue(), nullValue())
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(), nullValue())
        assertThat(saveReminderViewModel.selectedPOI.getOrAwaitValue(), nullValue())
        assertThat(saveReminderViewModel.latitude.getOrAwaitValue(), nullValue())
        assertThat(saveReminderViewModel.longitude.getOrAwaitValue(), nullValue())

    }

    @Test
    fun saveReminder_equalToDataSource() = mainCoroutineRule.runBlockingTest {

        fakeDataSource.deleteAllReminders()

        var reminder = ReminderDataItem(
            "Mardin",
            "memory with my family",
            "Cag Urfa Sofrası",
            36.79,
            37.45
        )

        saveReminderViewModel.saveReminder(reminder)

        val reminderFromDataSource = (fakeDataSource.getReminders() as Result.Success).data

        assertThat(reminder.title, equalTo(reminderFromDataSource[0].title))
    }

    @Test
    fun saveReminder_loading() = mainCoroutineRule.runBlockingTest {

        var reminder = ReminderDataItem(
            "Mardin",
            "memory with my family",
            "Cag Urfa Sofrası",
            36.79,
            37.45
        )
        mainCoroutineRule.pauseDispatcher()

        saveReminderViewModel.saveReminder(reminder)


        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))

    }

    @Test
    fun saveReminder_toastMessage() = mainCoroutineRule.runBlockingTest{

        var reminder = ReminderDataItem(
            "Mardin",
            "memory with my family",
            "Cag Urfa Sofrası",
            36.79,
            37.45
        )

        saveReminderViewModel.saveReminder(reminder)

        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`(
            getApplicationContext<Context>()
                .getString(R.string.reminder_saved))
        )

    }

}