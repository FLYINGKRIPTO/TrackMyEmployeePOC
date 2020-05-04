package trackemployee.io.workmanager.ui.maps
import android.Manifest
import android.content.IntentSender
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import com.google.android.gms.maps.model.MarkerOptions
import trackemployee.io.workmanager.R
import trackemployee.io.workmanager.data.models.Response
import trackemployee.io.workmanager.data.persistence.Location
import trackemployee.io.workmanager.ui.base.BaseActivity
import trackemployee.io.workmanager.ui.main.MainViewModel
import trackemployee.io.workmanager.utility.extentions.*
import trackemployee.io.workmanager.utility.extentions.getViewModel
import trackemployee.io.workmanager.utility.makeStatusNotification
import trackemployee.io.workmanager.viewmodel.ViewModelFactory
import javax.inject.Inject

@RuntimePermissions
class AddBookmarkLocation : BaseActivity() {

    private lateinit var mapFragment : SupportMapFragment
    private lateinit var googleMap: GoogleMap
    @Inject lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel : MainViewModel

    private lateinit var saveBtn  : Button
    private lateinit var editTextBookmark : EditText
    private  var latitude : Double? = null
    private var longitude : Double? = null

    companion object {
        const val REQUEST_CHECK_SETTINGS = 100
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_bookmark_location)

        viewModel = getViewModel(MainViewModel::class.java, viewModelFactory = viewModelFactory)

        saveBtn = findViewById(R.id.save_bookmark)
        editTextBookmark = findViewById(R.id.location_name)

        saveBtn.setOnClickListener {
            viewModel.saveLocation(Location(id = 0,landmark = editTextBookmark.text.toString(),longitude = longitude!!,latitude = latitude!!, timestamp = System.currentTimeMillis()))

        }
        getFromLocationWithPermissionCheck()
        observe(viewModel.enableLocation) {
            it ?: return@observe
            when (it.status) {
                Response.Status.LOADING -> toast("Loading")
                Response.Status.SUCCESS -> getFromLocationWithPermissionCheck()
                Response.Status.ERROR -> {
                    if (it.error is ResolvableApiException) {
                        try {
                            it.error.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
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
                    getMapAsync()
                    it.data ?: return@observe

                }
                Response.Status.ERROR -> {
                    toast("Error loading location")
                }
            }
        }
        mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment


    }
    fun getMapAsync(){
        mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(OnMapReadyCallback {
            googleMap = it
            if ( application.isGPSEnabled() &&  application.checkLocationPermission()) {
                LocationServices.getFusedLocationProviderClient(application)
                        ?.lastLocation
                        ?.addOnSuccessListener { location: android.location.Location? ->
                            if (location != null) {
                                val markerOptions= MarkerOptions().apply {
                                    val latLng = LatLng(location.latitude,location.longitude)
                                    position(latLng)
                                    title("Your Current Location")
                                    googleMap.clear()
                                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                                    googleMap.addMarker(this)
                                    latitude = location.latitude
                                    longitude = location.longitude
                                }
                            }
                        }?.addOnFailureListener{
                            makeStatusNotification("Failed due to : ${it.localizedMessage}", this)
                        }
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode,grantResults)
    }
    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun getFromLocation() = if (isGPSEnabled()) getMapAsync() else viewModel.locationSetup()

}
