package org.esiea.cam_gidon.kotlineval

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Toast
import layout.location
import layout.venue
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var json: String
    private lateinit var obj: JSONObject
    lateinit var locationManager: LocationManager
    private var hasGps = false
    private var hasNetwork = false
    private var locationGps : Location? = null
    private var locationNetwork : Location? = null
    private lateinit var listVenue: List<venue>

    private val client = OkHttpClient()

    private var permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.INTERNET)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Asking permissions
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, 10)
        }

        // Set location
        getLocation()
        val lat = locationGps!!.latitude
        val long = locationGps!!.longitude
        Toast.makeText(this, "Latitude : " + lat + ", Longitude : " + long, Toast.LENGTH_LONG).show()

        // Call api to get all sushi near us
        run("https://api.foursquare.com/v2/venues/search?client_id=AYPL0UXZZ1WEQRVQV0S5KGRHIIRBCTQN4QCUSBZZV2CDO1SI&client_secret=MAQ0CIXWCFUU1SXFYLL1EDP5PDFIDA2BR40JBHDLWEJFOQKR &ll="+lat+","+long+"&query=sushi&v=20190412")

        // Mock
        listVenue = mutableListOf(venue(id = "4b8d6ce7f964a52006fb32e3", name = "SushiShop", distance = "13", location = location(address = "24 rue des beaunes", postalCode = "78400", country = "France", city = "Chatou"), rate = 3),
            venue(id = "4b8d6ce7f964a52006fb32e2", name = "Bento", distance = "30", location = location(address = "3 rue desmoulins", postalCode = "78150", country = "France", city = "Chatou"), rate = 7),
            venue(id = "4b8d6ce7f964a52006fb32e1", name = "Pizza pour tous", distance = "20", location = location(address = "24 rue des pizza", postalCode = "78400", country = "France", city = "Chatou"), rate = 10),
            venue(id = "4b8d6ce7f964a52006fb32e0", name = "Super U", distance = "8", location = location(address = "24 rue des souris", postalCode = "78400", country = "France", city = "Chatou"), rate = 5))

        //Call api get rate for each venue
        listVenue.forEach() {
            //Call api to get rate with it.id
            run("https://api.foursquare.com/v2/venues/" + it.id +"?client_id=AYPL0UXZZ1WEQRVQV0S5KGRHIIRBCTQN4QCUSBZZV2CDO1SI&client_secret=MAQ0CIXWCFUU1SXFYLL1EDP5PDFIDA2BR40JBHDLWEJFOQKR &ll="+lat+","+long+"&query=sushi&v=20190412")
            //fill it with the rate from the API
            //TODO
            listVenue.find { x -> x.id == it.id }?.rate = (Math.random() * 10).toInt(); // Random for fun
        }

        // Set Adapter
        viewManager = LinearLayoutManager(this)
        viewAdapter = MyAdapter(listVenue)

        // Create recycler
        val recycler = findViewById(R.id.recycler) as RecyclerView
        recycler.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

        }
    }

    fun run(url: String) {
        // Create request with OkHttp
        val request = Request.Builder()
            .url(url)
            .build()
        //send request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                // Make an object from the json received
                json = response.body()?.string() ?: ""
                obj = JSONObject(json)
            }
//            override fun onResponse(call: Call, response: Response) = println(response.body()?.string())
        })
    }

    //Method to get location
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if(hasGps || hasNetwork) {
            if(hasGps) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0F, object : LocationListener {
                    override fun onLocationChanged(location: Location?) {
                        if(location!=null) {
                            locationGps = location
                        }
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

                    override fun onProviderEnabled(provider: String?) {}

                    override fun onProviderDisabled(provider: String?) {}

                })
                val localGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if(localGpsLocation!=null) {
                    locationGps = localGpsLocation
                }
            }
            if(hasNetwork) {
                Log.d("CodeAndroidLocation", "hasNetwork")
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0F, object : LocationListener {
                    override fun onLocationChanged(location: Location?) {
                        if(location!=null) {
                            locationNetwork = location
                        }
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) { }

                    override fun onProviderEnabled(provider: String?) { }

                    override fun onProviderDisabled(provider: String?) {}

                })
                val localNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if(localNetworkLocation!=null) {
                    locationNetwork = localNetworkLocation
                }
            }
        } else {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }
}


