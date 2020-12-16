package com.example.android.vjournalcalendar.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.*

import com.example.android.vjournalcalendar.database.VJournalDatabaseDao
import com.example.android.vjournalcalendar.database.vJournalItem
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


/**
 * ViewModel for SleepTrackerFragment.
 */
class VJournalListViewModel(
        val database: VJournalDatabaseDao,
        application: Application) : AndroidViewModel(application) {

        var vJournalFocusItem: MutableLiveData<vJournalItem> = MutableLiveData<vJournalItem>().apply { vJournalItem()  }
        var filterString: MutableLiveData<String> = MutableLiveData<String>()
        var vJournalList: LiveData<List<vJournalItem>> = Transformations.switchMap(filterString) { filter ->
            if (filter.isNullOrBlank() || filter == "%")
                database.getVJournalItems()
            else
                database.getVJournalItems("%${filter.replace(" ", "%")}%")
            // Note: The tranformation could not handle multiple method calls in order to separate searches for
        }

        val allCategories: LiveData<List<String>> = database.getAllCategories()

    init {

        viewModelScope.launch {
            insertTestData()

        }
    }



    private suspend fun insertTestData() {

        val lipsumSummary = "Lorem ipsum dolor sit amet"
        val lipsumDescription = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."


        database.insert(vJournalItem(0L, lipsumSummary, lipsumDescription, System.currentTimeMillis(), "Organizer", "UID", "#category1, #category2", "FINAL","PUBLIC", "", System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis(), 0))
        database.insert(vJournalItem(0L, lipsumSummary, lipsumDescription, System.currentTimeMillis(), "Organizer", "UID", "#category1, #category2", "FINAL","PUBLIC", "", System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis(), 0))
    }



    fun setFocusItem(vJournalItemId: Long) {

        vJournalFocusItem.value = vJournalList.value?.find { focusItem ->
            focusItem.id == vJournalItemId
        }!!

    }

    fun getFocusItemPosition(): Int? {
        return vJournalList.value?.indexOf(vJournalFocusItem.value)
    }

    fun setFilter(filter: String) {
        filterString.value = filter
    }

}
