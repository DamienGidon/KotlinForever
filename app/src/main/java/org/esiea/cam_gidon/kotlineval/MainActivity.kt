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
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var json: String
    lateinit var locationManager: LocationManager
    private var hasGps = false
    private var hasNetwork = false
    private var locationGps : Location? = null
    private var locationNetwork : Location? = null
    private lateinit var listVenue: MutableList<venue>

    private val client = OkHttpClient()

    private var permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.INTERNET)

    override fun onCreate(savedInstanceState: Bundle?) {
        listVenue = mutableListOf();
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
        run("https://api.foursquare.com/v2/venues/search?client_id=3S5XNUMUDPXQH2AUVFIHDGTUDUIIDBN3FHU3J5P3YQ52GJ45&client_secret=OTN5IVWPAFQGLE3C0RRPKRKOH55SW102HMYVXJWABDR2ULN2&ll="+lat+","+long+"&query=sushi&v=20190412&limit=7")

        do {
            Thread.sleep(1000)
        } while (listVenue.count() < 1)

        //Call api get rate for each venue
        listVenue.forEach() {
            //Call api to get rate with it.id
            runWithID("https://api.foursquare.com/v2/venues/" + it.id +"?client_id=3S5XNUMUDPXQH2AUVFIHDGTUDUIIDBN3FHU3J5P3YQ52GJ45&client_secret=OTN5IVWPAFQGLE3C0RRPKRKOH55SW102HMYVXJWABDR2ULN2&ll="+lat+","+long+"&query=sushi&v=20190412", it.id)
        }

        Thread.sleep(2000)

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
                val obj = JSONObject(json)
                val response = JSONObject(obj.getString("response"))
                val jsonList = response.getJSONArray("venues")
                for (i in 0 until jsonList.length()) {

                    val test =  jsonList.getJSONObject(i)
                    val id = test.getString("id")?: ""
                    val name = test.getString("name")?: ""
                    val locJson = JSONObject(test.getString("location"))
                    var address = ""
                    if (locJson.has("address")) {
                       address  = locJson.getString("address")
                    }
                    var city = ""
                    if (locJson.has("city")) {
                        city  = locJson.getString("city")
                    }
                    var country = ""
                    if (locJson.has("country")) {
                        country  = locJson.getString("country")
                    }
                    var postalCode = ""
                    if (locJson.has("postalCode")) {
                        postalCode  = locJson.getString("postalCode")
                    }
                    // Add object to the list
                    listVenue.add(venue(id = id, name = name ,distance = (Math.random() * 10).toInt().toString(),location =location(address = address ?: "", postalCode = postalCode ?: "", country = country ?: "", city = city ?: ""), rate = 5  ))
                }
            }
        })
    }

    fun runWithID(url: String, id: String) {
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
                val obj = JSONObject(json)
                val response = JSONObject(obj.getString("response"))
                val json = response.getJSONObject("venue")
                var rate = -1
                if (json.has("rating")) {
                    rate  = json.getInt("rating")
                }
                // update list venue
                listVenue.find { x -> x.id == id }?.rate = rate
            }
        })
    }

    //Method to get location
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if(hasGps || hasNetwork) {
            // position mobile connection
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
            // position internet connection
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


