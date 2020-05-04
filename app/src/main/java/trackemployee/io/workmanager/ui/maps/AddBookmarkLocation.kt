package trackemployee.io.workmanager.ui.maps
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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
import trackemployee.io.workmanager.utility.makeStatusNotification
import trackemployee.io.workmanager.viewmodel.ViewModelFactory
import javax.inject.Inject

class AddBookmarkLocation : BaseActivity() {

    private lateinit var mapFragment : SupportMapFragment
    private lateinit var googleMap: GoogleMap
    @Inject lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel : MainViewModel

    private lateinit var saveBtn  : Button
    private lateinit var editTextBookmark : EditText
    private  var latitude : Double? = null
    private var longitude : Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_bookmark_location)

        viewModel = getViewModel(MainViewModel::class.java, viewModelFactory = viewModelFactory)

       saveBtn = findViewById(R.id.save_bookmark)
        editTextBookmark = findViewById(R.id.location_name)


        mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(OnMapReadyCallback {
           googleMap = it
            if ( application.checkLocationPermission()) {
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

        saveBtn.setOnClickListener {
            viewModel.saveLocation(Location(id = 0,landmark = editTextBookmark.text.toString(),longitude = longitude!!,latitude = latitude!!, timestamp = System.currentTimeMillis()))

        }
    }
}
