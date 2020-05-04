package trackemployee.io.workmanager.utility.workmanager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import trackemployee.io.workmanager.data.repository.Repository
import trackemployee.io.workmanager.di.module.Provider
import timber.log.Timber
import trackemployee.io.workmanager.utility.makeStatusNotification
import javax.inject.Inject

class TrackLocationWorker @Inject constructor(
        context: Context,
        workerParams: WorkerParameters
) : Worker(context, workerParams) {

    @Inject lateinit var repository: Repository

    init {
        Provider.appComponent?.inject(this)
    }

    override fun doWork(): Result {
        return try {
            val appContext = applicationContext
            repository.location.getLocation(appContext)
            Result.Success.success()
        } catch (e: Exception) {
            Timber.e(e, "Failure in doing work")
            Result.Success.success()
        }
    }
}