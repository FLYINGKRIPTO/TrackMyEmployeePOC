package trackemployee.io.workmanager.data.repository

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import com.google.android.gms.location.LocationServices
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import trackemployee.io.workmanager.data.persistence.Database
import trackemployee.io.workmanager.data.persistence.Location
import trackemployee.io.workmanager.ui.base.BaseViewModel
import trackemployee.io.workmanager.ui.main.MainViewModel
import trackemployee.io.workmanager.utility.extentions.checkLocationPermission
import trackemployee.io.workmanager.utility.extentions.isGPSEnabled
import javax.inject.Inject
import javax.inject.Singleton
import trackemployee.io.workmanager.utility.*
import trackemployee.io.workmanager.utility.extentions.addTo
import trackemployee.io.workmanager.utility.extentions.fromWorkerToMain

@Singleton
class LocationRepository @Inject constructor(
        private val application: Application,
        private val database: Database,
        private val scheduler: trackemployee.io.workmanager.utility.rx.Scheduler
) {

    @SuppressLint("MissingPermission")
    fun getLocation(context : Context) {
        /*
         * One time location request
         */
        if ( application.isGPSEnabled() && application.checkLocationPermission()) {
            var latitude : Double? = null
            var longitude : Double? = null
            var result : FloatArray = FloatArray(2).apply {
                this.plus(13.33F)
            }
            LocationServices.getFusedLocationProviderClient(application)
                    ?.lastLocation
                    ?.addOnSuccessListener { location: android.location.Location? ->
                        if (location != null) {
                            getSavedLocation().subscribeBy(
                                    onNext = {
                                        latitude = it[0].latitude
                                        longitude = it[0].longitude
                                        Timber.e("latitude : $latitude :::::: longitude : $longitude")
                                        Timber.e(" DISTANCE ${latitude}  :::: ${location.latitude}")
                                        Timber.e(" DISTANCE ${longitude} :::: ${location.longitude}")
                                        android.location.Location.distanceBetween(latitude!!,longitude!!,location.latitude,location.longitude, result )
                                        val distanceInMeters = result.last()
                                        Timber.e("DISTANCE IN METERS ${result.size}  $distanceInMeters")
                                        when {
                                            distanceInMeters > 50.0 -> {
                                                //todo: logout --> stop the work
                                                WorkManager.getInstance().cancelAllWorkByTag(MainViewModel.LOCATION_WORK_TAG)
                                                makeStatusNotification("Outside the circle $distanceInMeters", context = context)
                                            }
                                            distanceInMeters <= 50.0 -> {
                                                makeStatusNotification("Inside the circle $distanceInMeters", context = context)
                                            }
                                        }

                                    },
                                    onError = {
                                        makeStatusNotification("Error", context = context)

                                        Timber.e(it.localizedMessage)
                                    },
                                    onComplete = {
                                        Timber.e(" DISTANCE ${latitude}  :::: ${location.latitude}")
                                        Timber.e(" DISTANCE ${longitude} :::: ${location.longitude}")
                                        android.location.Location.distanceBetween(latitude!!,longitude!!,location.latitude,location.longitude, result )
                                        val distanceInMeters = result.last()
                                        Timber.e("DISTANCE IN METERS ${result.size}  $distanceInMeters")
                                        when {
                                            distanceInMeters > 50.0 -> {
                                                makeStatusNotification("Outside the circle $distanceInMeters" , context = context)
                                            }
                                            distanceInMeters <= 50.0 -> {
                                                makeStatusNotification("Inside the circle  $distanceInMeters ", context = context)
                                            }
                                        }
                                    }
                            )
                        }
                    }
        }
    }

    fun saveLocation(location: Location) = GlobalScope.launch { database.locationDao().insert(location) }

    fun getSavedLocation(): Flowable<List<Location>> =   database.locationDao().selectAll()

}