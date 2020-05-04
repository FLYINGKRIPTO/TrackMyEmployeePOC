package trackemployee.io.workmanager.ui.main

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import androidx.work.WorkManager
import com.google.android.gms.common.api.ResolvableApiException
import kotlinx.android.synthetic.main.activity_main.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import trackemployee.io.workmanager.R
import trackemployee.io.workmanager.data.models.Response
import trackemployee.io.workmanager.ui.base.BaseActivity
import trackemployee.io.workmanager.utility.RxBus
import trackemployee.io.workmanager.utility.extentions.*
import trackemployee.io.workmanager.viewmodel.ViewModelFactory
import timber.log.Timber
import trackemployee.io.workmanager.ui.maps.AddBookmarkLocation
import javax.inject.Inject

@RuntimePermissions
class MainActivity : BaseActivity() {

    @Inject lateinit var viewModelFactory: ViewModelFactory
    @Inject lateinit var bus: RxBus<Any>

    private lateinit var viewModel: MainViewModel
    private lateinit var mAdapter: MainAdapter

    companion object {
        const val REQUEST_CHECK_SETTINGS = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = getViewModel(MainViewModel::class.java, viewModelFactory)
        viewModel.getSavedLocation()

        mAdapter = MainAdapter(this, listOf())
        rvLocation.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = mAdapter
        }

        btnTrack.setOnClickListener { getFromLocationWithPermissionCheck() }
        btnStop.setOnClickListener { viewModel.stopTrackLocation() }
        btnSaveLocation.setOnClickListener {
            val intent = Intent(this,AddBookmarkLocation::class.java)
            startActivity(intent)

        }


        observe(viewModel.enableLocation) {
            it ?: return@observe
            when (it.status) {
                Response.Status.LOADING -> toast("Loading")
                Response.Status.SUCCESS -> getFromLocationWithPermissionCheck()
                Response.Status.ERROR -> {
                    if (it.error is ResolvableApiException) {
                        try {
                            it.error.startResolutionForResult(this@MainActivity, REQUEST_CHECK_SETTINGS)
                        } catch (sendEx: IntentSender.SendIntentException) {

                        }
                    }
                }
            }
        }

        observe(viewModel.location) {
            it ?: return@observe
            when (it.status) {
                Response.Status.LOADING -> {

                }
                Response.Status.SUCCESS -> {
                    it.data ?: return@observe
                    mAdapter.locations = it.data
                }
                Response.Status.ERROR -> {
                    toast("Error loading location")
                }
            }
        }

        observe(WorkManager.getInstance().getWorkInfosByTagLiveData(MainViewModel.LOCATION_WORK_TAG)){
            it ?: return@observe
            if(it.isEmpty()) return@observe
            val status = it[0].state.name
            tvWorkStatus.apply {
                text = "WorkManagerStatus : $status"
                visible = true
            }
            Timber.d("Work Manager Status $status")
        }
//        observe(WorkManager.getInstance().getStatusesByTagLiveData(MainViewModel.LOCATION_WORK_TAG)) {
//            it ?: return@observe
//            if (it.isEmpty()) return@observe
//            val status = it[0].state.name
//            tvWorkStatus.apply {
//                text = "Work Manager Status: $status"
//                visible = true
//            }
//            Timber.d("Work Manager Status: $status")
//        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun getFromLocation() = if (isGPSEnabled()) viewModel.trackLocation() else viewModel.locationSetup()

}
