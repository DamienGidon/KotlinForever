package layout

data class venue(val id: String, val name: String, val distance: String, var rate: Int, val location: location)

data class location(val address: String, val city: String, val country: String, val postalCode: String)