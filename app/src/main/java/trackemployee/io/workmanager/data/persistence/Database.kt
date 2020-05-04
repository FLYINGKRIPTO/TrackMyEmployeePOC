package trackemployee.io.workmanager.data.persistence

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [
    Location::class
], version = 1)
abstract class Database : RoomDatabase() {
    abstract fun locationDao(): LocationDao
}