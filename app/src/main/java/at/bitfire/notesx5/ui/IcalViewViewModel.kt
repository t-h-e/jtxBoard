package at.bitfire.notesx5.ui

import android.app.Application
import androidx.lifecycle.*
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.properties.Attendee
import at.bitfire.notesx5.database.properties.Category
import at.bitfire.notesx5.database.properties.Relatedto
import at.bitfire.notesx5.database.relations.ICalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*


class IcalViewViewModel(private val vJournalItemId: Long,
                        val database: ICalDatabaseDao,
                        application: Application) : AndroidViewModel(application) {

    lateinit var vJournal: LiveData<ICalEntity?>
    lateinit var categories: LiveData<List<Category>>
    lateinit var attendees: LiveData<List<Attendee>>
    lateinit var relatedNotes: LiveData<List<ICalObject?>>
    lateinit var relatedSubtasks: LiveData<List<ICalObject?>>


    lateinit var dateVisible: LiveData<Boolean>
    lateinit var timeVisible: LiveData<Boolean>
    lateinit var dtstartFormatted: LiveData<String>
    lateinit var createdFormatted: LiveData<String>
    lateinit var lastModifiedFormatted: LiveData<String>

    lateinit var urlVisible: LiveData<Boolean>
    lateinit var attendeesVisible: LiveData<Boolean>
    lateinit var organizerVisible: LiveData<Boolean>
    lateinit var contactVisible: LiveData<Boolean>
    lateinit var commentsVisible: LiveData<Boolean>
    lateinit var relatedtoVisible: LiveData<Boolean>
    lateinit var progressVisible: LiveData<Boolean>
    lateinit var priorityVisible: LiveData<Boolean>
    lateinit var subtasksVisible: LiveData<Boolean>


    var editingClicked: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) }


    init {

        viewModelScope.launch {

            // insert a new value to initialize the vJournalItem or load the existing one from the DB
            vJournal = if (vJournalItemId == 0L)
                MutableLiveData<ICalEntity?>().apply {
                    postValue(ICalEntity(ICalObject(), null, null, null, null, null))
                }
            else
                database.get(vJournalItemId)


            categories = Transformations.map(vJournal) {
                it?.category
            }

            attendees = Transformations.map(vJournal) {
                it?.attendee
            }


            relatedNotes = Transformations.switchMap(vJournal) {
                it?.property?.id?.let { parentId -> database.getRelatedNotes(parentId) }
            }

            relatedSubtasks = Transformations.switchMap(vJournal) {
                it?.property?.id?.let { parentId -> database.getRelatedTodos(it.property.id) }
            }


            dateVisible = Transformations.map(vJournal) { item ->
                return@map item?.property?.component == "JOURNAL"           // true if component == JOURNAL
            }

            timeVisible = Transformations.map(vJournal) { item ->
                return@map item?.property?.component == "JOURNAL" && item?.property.dtstartTimezone != "ALLDAY"           // true if component == JOURNAL and it is not an All Day Event

            }

            dtstartFormatted = Transformations.map(vJournal) { item ->
                if (item!!.property.dtstart != null) {
                    val formattedDate = DateFormat.getDateInstance(DateFormat.LONG).format(Date(item.property.dtstart!!))
                    val formattedTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(item.property.dtstart!!))
                    return@map "$formattedDate $formattedTime"
                } else
                    return@map ""

            }

            createdFormatted = Transformations.map(vJournal) { item ->
                item!!.property.let { Date(it.created).toString() }
            }

            lastModifiedFormatted = Transformations.map(vJournal) { item ->
                item!!.property.let { Date(it.lastModified).toString() }
            }




            urlVisible = Transformations.map(vJournal) { item ->
                return@map !item?.property?.url.isNullOrBlank()      // true if url is NOT null or empty
            }
            attendeesVisible = Transformations.map(vJournal) { item ->
                return@map !item?.attendee.isNullOrEmpty()      // true if attendees is NOT null or empty
            }
            organizerVisible = Transformations.map(vJournal) { item ->
                return@map !(item?.organizer == null)      // true if organizer is NOT null or empty
            }
            contactVisible = Transformations.map(vJournal) { item ->
                return@map !item?.property?.contact.isNullOrBlank()      // true if contact is NOT null or empty
            }
            relatedtoVisible = Transformations.map(vJournal) { item ->
                return@map !item?.relatedto.isNullOrEmpty()      // true if relatedto is NOT null or empty
            }
            subtasksVisible = Transformations.map(relatedSubtasks) { subtasks ->
                return@map subtasks?.isNotEmpty()      // true if relatedto is NOT null or empty
            }
            commentsVisible = Transformations.map(vJournal) { item ->
                return@map !item?.comment.isNullOrEmpty()      // true if relatedto is NOT null or empty
            }
            progressVisible = Transformations.map(vJournal) { item ->
                return@map item?.property?.percent != null && item.property.component == "TODO"     // true if percent (progress) is NOT null
            }
            priorityVisible = Transformations.map(vJournal) { item ->
                return@map item?.property?.priority != null      // true if priority is NOT null
            }

        }
    }

    fun editingClicked() {
        editingClicked.value = true
    }


    fun insertRelatedNote(note: ICalObject) {
        viewModelScope.launch() {
            val newNoteId = database.insertJournal(note)
            database.upsertRelatedto(Relatedto(icalObjectId = vJournal.value!!.property.id, linkedICalObjectId = newNoteId, reltypeparam = "CHILD", text = note.uid))

        }
    }

    fun deleteNote(note: ICalObject) {
        viewModelScope.launch(Dispatchers.IO) {
            //todo delete also link in relatedto!
            database.delete(note)
        }
    }


    fun updateProgress(item: ICalObject, newPercent: Int) {

        val item2update = item
        item2update.percent = newPercent
        item2update.lastModified = System.currentTimeMillis()

        viewModelScope.launch() {
            database.update(item2update)
        }
    }
}



