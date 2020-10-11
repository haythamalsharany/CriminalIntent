package com.alsharany.criminalintent.database

import android.animation.TypeConverter
import androidx.room.TypeConverters
import java.util.*

class CrimeTypeConverter  {

    @TypeConverters
    fun toDate(millisSinceEpoch: Long?): Date? {
        return millisSinceEpoch?.let {
            Date(it)
        }
    }
    @TypeConverters
    fun fromDate(date:Date?):Long?{
        return date?.time
    }
    @TypeConverters
    fun toUUID(uuid:String?):UUID?{
        return UUID.fromString(uuid)
    }
    @TypeConverters
    fun fromUUID(uuid:UUID?):String?{
        return uuid?.toString()
    }
}