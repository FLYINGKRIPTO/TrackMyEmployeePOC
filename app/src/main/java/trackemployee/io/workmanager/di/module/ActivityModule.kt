package trackemployee.io.workmanager.di.module

import dagger.Module
import dagger.android.ContributesAndroidInjector
import trackemployee.io.workmanager.di.ActivityScoped
import trackemployee.io.workmanager.ui.main.MainActivity
import trackemployee.io.workmanager.ui.maps.AddBookmarkLocation

@Module
abstract class ActivityModule {

    @ActivityScoped
    @ContributesAndroidInjector(modules = [])
    abstract fun mainActivity(): MainActivity
    @ContributesAndroidInjector
    abstract fun addBookmarkLocation() : AddBookmarkLocation

}