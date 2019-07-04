package org.esiea.cam_gidon.kotlineval

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import layout.location
import layout.venue
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var json: String

    private val client = OkHttpClient()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Call api
        val call = run("https://api.foursquare.com/v2/venues/search?client_id=AYPL0UXZZ1WEQRVQV0S5KGRHIIRBCTQN4QCUSBZZV2CDO1SI&client_secret=MAQ0CIXWCFUU1SXFYLL1EDP5PDFIDA2BR40JBHDLWEJFOQKR &ll=40.7,-74&query=sushi&v=20190412")

        // Mock
        val t = listOf(venue(name = "tour", distance = "13km", location = location(address = "24 rue des beaunes", postalCode = "78400", country = "France", city = "Chatou"), rate = 10),
            venue(name = "rat", distance = "30km", location = location(address = "24 rue des beaunes", postalCode = "78400", country = "France", city = "Paris"), rate = 10))

        // Set Adapter
        viewManager = LinearLayoutManager(this)
        viewAdapter = MyAdapter(t)

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
        // Create request
        val request = Request.Builder()
            .url(url)
            .build()
        //send request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                json = response.body()?.string() ?: ""
            }
//            override fun onResponse(call: Call, response: Response) = println(response.body()?.string())
        })
    }
}
