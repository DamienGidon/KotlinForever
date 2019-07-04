package layout

data class venue(val name: String, val distance: String, val rate: Int, val location: location)

data class location(val address: String, val city: String, val country: String, val postalCode: String)