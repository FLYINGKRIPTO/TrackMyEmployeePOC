package trackemployee.io.workmanager

import com.facebook.stetho.Stetho
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import trackemployee.io.workmanager.di.component.DaggerAppComponent
import trackemployee.io.workmanager.di.module.Provider
import trackemployee.io.workmanager.utility.logging.CrashReportingTree
import timber.log.Timber

class WorkManagerApplication : DaggerApplication() {

    override fun onCreate() {
        super.onCreate()

        Stetho.initializeWithDefaults(this)
        Timber.plant(if (BuildConfig.DEBUG) Timber.DebugTree() else CrashReportingTree())
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        val appComponent = DaggerAppComponent.builder().application(this).build()
        appComponent.inject(this)
        Provider.appComponent = appComponent
        return appComponent
    }
}
