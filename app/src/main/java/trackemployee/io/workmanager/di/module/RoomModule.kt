package trackemployee.io.workmanager.di.module

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import trackemployee.io.workmanager.data.persistence.Database
import javax.inject.Singleton

@Module
class RoomModule {

    @Provides
    @Singleton
    fun provideDatabase(application: Application): Database =
            Room.databaseBuilder(application, Database::class.java, "WorkManager.db").build()
}