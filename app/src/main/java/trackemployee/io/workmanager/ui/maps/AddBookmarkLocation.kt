package trackemployee.io.workmanager.ui.maps
import android.os.Bundle
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import trackemployee.io.workmanager.R
import trackemployee.io.workmanager.data.persistence.Location
import trackemployee.io.workmanager.ui.base.BaseActivity
import trackemployee.io.workmanager.ui.main.MainViewModel
import trackemployee.io.workmanager.utility.extentions.checkLocationPermission
import trackemployee.io.workmanager.utility.extentions.getViewModel
import trackemployee.io.workmanager.utility.extentions.isGPSEnabled
import trackemployee.io.workmanager.viewmodel.ViewModelFactory
import javax.inject.Inject

class AddBookmarkLocation : BaseActivity() {

    private lateinit var mapFragment : SupportMapFragment
    private lateinit var googleMap: GoogleMap
    @Inject lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel : MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_bookmark_location)

        viewModel = getViewModel(MainViewModel::class.java, viewModelFactory = viewModelFactory)


        mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(OnMapReadyCallback {
           googleMap = it
            if (application.isGPSEnabled() && application.checkLocationPermission()) {
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
                                    viewModel.saveLocation(Location(id = 0,landmark = "Office",longitude = location.longitude,latitude = location.latitude, timestamp = System.currentTimeMillis()))
                                }
                            }
                        }
            }
        })
    }
}
