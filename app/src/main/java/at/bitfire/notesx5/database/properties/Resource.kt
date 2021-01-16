package at.bitfire.notesx5.database.properties


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import at.bitfire.notesx5.database.ICalObject
import kotlinx.android.parcel.Parcelize


@Parcelize
@Entity(tableName = "resource",
        foreignKeys = [ForeignKey(entity = ICalObject::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("icalObjectId"),
                onDelete = ForeignKey.CASCADE)])
data class Resource (

        @PrimaryKey(autoGenerate = true)
        var id: Long = 0L,
        var icalObjectId: Long = 0L,
        var text: String = "",
        var reltypeparam: String? = null,
        var otherparam: String? = null
): Parcelable



