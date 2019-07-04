package org.esiea.cam_gidon.kotlineval

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import layout.location
import layout.venue

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Mock
        val t = listOf(venue(name = "tour", distance = "13km", location = location(address = "24 rue des beaunes", postalCode = "78400", country = "France", city = "Chatou"), rate = 10),
            venue(name = "rat", distance = "30km", location = location(address = "24 rue des beaunes", postalCode = "78400", country = "France", city = "Paris"), rate = 10))
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


}
