package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {


    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    @Before
    fun setup() {
        stopKoin()
        appContext = getApplicationContext()
        startKoin {
            androidContext(appContext)
            modules(listOf(testModules))
        }

        repository = get()

        runBlocking {
            repository.deleteAllReminders()
        }
    }

    private val testModules = module {
        // single<Application> { getApplicationContext() }   // if you want Application instance, getApplicationContext() will give you it.
        // single<Context> { getApplicationContext() }      //  if you want Application instance, getApplicationContext() will give you it.

        viewModel {
            RemindersListViewModel(
                appContext, // Application instance
                get()    // if you want instance of ReminderDataSource interface, i will give you instance of RemindersLocalRepository.
            )
        }

        single {
            SaveReminderViewModel(
                appContext, // Application instance
                get()  // if you want instance of ReminderDataSource interface, i will give you instance of RemindersLocalRepository.
            )
        }

        single { LocalDB.createRemindersDao(appContext) }  // if you want instance of RemindersDao, i will give you it by calling LocalDB.createRemindersDao( context )
        /*alternatively you write an in-memory roomdb as the following
        * single {
            Room.inMemoryDatabaseBuilder(
                    getApplicationContext(),
                    RemindersDatabase::class.java
            ).allowMainThreadQueries().build().reminderDao()
        }
        * */

        single<ReminderDataSource> { RemindersLocalRepository(get()) }   // if you want instance of ReminderDataSource interface, i will give you instance of RemindersLocalRepository.
        // single{ RemindersLocalRepository(get()) as ReminderDataSource } // you can do the above line with this line instead. They are the same.
    }

    @After
    fun tearDown() {
        stopKoin()
    }


    //At first launch of ReminderListFragment, there is no reminder. Thus "No Data" is shown.
    @Test
    fun thereIsNoReminder_checkNoData_isDisplayed() {
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(withId(R.id.noDataTextView)).check(
            matches(isDisplayed())
        )
    }

    @Test
    fun clickFAB_navigateToSaveReminderFragment() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
        }

        onView(withId(R.id.addReminderFAB)).perform(click())

        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }


    @Test
    fun listFragment_reminderIsDisplayed() {

        val reminder = ReminderDTO(
            "Mardin",
            "memory with my family",
            "Cag Urfa Sofrası",
            36.79,
            37.45,
            "123"
        )

        runBlocking {
            repository.saveReminder(reminder)
        }

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(withText(reminder.title)).check(ViewAssertions.matches(isDisplayed()))
        onView(withText(reminder.description)).check(ViewAssertions.matches(isDisplayed()))

    }

    @Test
    fun deleteAllOption_checkDisplayNoData() {

        val reminder = ReminderDTO(
            "Mardin",
            "memory with my family",
            "Cag Urfa Sofrası",
            36.79,
            37.45,
            "123"
        )

        runBlocking {
            repository.saveReminder(reminder)
        }

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        runBlocking {
            repository.deleteAllReminders()
        }

        onView(withId(R.id.refreshLayout)).perform(swipeDown())
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }
}

