package com.example.testarmflow.mapFrag

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.testarmflow.CompClassesChat.Users
import com.example.testarmflow.CompClassesMap.Events
import com.example.testarmflow.CompClassesMap.InfoWindowAdapter
import com.example.testarmflow.R
import com.example.testarmflow.databinding.FragmentMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentMapsBinding
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val zoom: Float = 10F
    private lateinit var database: FirebaseDatabase
    private lateinit var ref: DatabaseReference
    private lateinit var userRef: DatabaseReference
    private var marker: Marker? = null
    private lateinit var markerOptions: MarkerOptions
    private lateinit var mEvents: List<Events>
    private lateinit var user: List<Users>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = FirebaseDatabase.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMapsBinding.inflate(inflater, container, false)

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.apply {
            mapMenu.setOnClickListener {
                binding.optionsLayout.visibility = View.VISIBLE
            }
            createEvent.setOnClickListener{
                val fragment = AddMarkerFragment()
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(binding.root.id, fragment)
                transaction.setReorderingAllowed(true)
                transaction.addToBackStack("fragments_map")
                transaction.commit()
            }
            myEvents.setOnClickListener {
                val fragment = MyEventsFragment()
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(binding.root.id, fragment)
                transaction.setReorderingAllowed(true)
                transaction.addToBackStack("fragments_map")
                transaction.commit()
            }

            backToMap.setOnClickListener {
                binding.optionsLayout.visibility = View.GONE
            }
        }
        ref = database.reference.child("events")
        userRef = database.reference.child("users")


        mapInitialize()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mEvents = ArrayList()
        user = ArrayList()
    }

    private fun mapInitialize() {
        var locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(2000)
            .setMaxUpdateDelayMillis(100)
            .build()

        binding.searchLocation.setOnEditorActionListener(object : OnEditorActionListener{
            override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
                if(p1 == EditorInfo.IME_ACTION_SEARCH
                    || p1 == EditorInfo.IME_ACTION_DONE
                    || p2!!.action == KeyEvent.ACTION_DOWN
                    || p2!!.action == KeyEvent.KEYCODE_ENTER) {
                    goToSearchLocation()
                }
                return false
            }
        })
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private fun goToSearchLocation() {
        val searchLocation: String = binding.searchLocation.text.toString()

        val geocoder = Geocoder(requireContext())
        var list: MutableList<Address> = mutableListOf()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocationName(searchLocation, 1, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    if (addresses != null && addresses.isNotEmpty()) {
                        val address = addresses[0]
                        val location = address.adminArea
                        val latitude = address.latitude
                        val longitude = address.longitude
                        goToLatLng(latitude, longitude, zoom)
                    }
                }
                override fun onError(errorMessage: String?) {
                    Log.e("GeoError", "GeoCode Error: ${errorMessage.toString()}")
                }
            })
        }
        else {
            try {
                list = geocoder.getFromLocationName(searchLocation, 1) as MutableList<Address>
            }
            catch(e: IOException){
                e.printStackTrace()
            }
            if(list.size > 0){
                val address : Address = list[0]
                val location = address.adminArea
                val latitude = address.latitude
                val longitude = address.longitude
                goToLatLng(latitude, longitude, zoom)
            }
        }
    }

    private fun goToLatLng(latitude: Double, longitude: Double, fl: Float) {
        val latLng = LatLng(latitude, longitude)
        binding.latitudeTxt.text = ("Szerokosc : " + latLng.latitude.toString())
        binding.longitudeTxt.text = ("Dlugosc : " + latLng.longitude.toString())
        val update = CameraUpdateFactory.newLatLngZoom(latLng, fl)
        mMap.animateCamera(update)
    }


    override fun onMapReady(map: GoogleMap) {
        mMap = map
        mMap.setOnCameraMoveListener {
            val location = mMap.cameraPosition.target

            binding.latitudeTxt.text = ("Szerokosc : " + location.latitude.toString())
            binding.longitudeTxt.text = ("Dlugosc : " + location.longitude.toString())
        }

        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                mMap.clear()
                (mEvents as ArrayList<Events>).clear()
                for(dataSnapshot in snapshot.children){
                    val event = dataSnapshot.getValue(Events::class.java)
                    (mEvents as ArrayList<Events>).add(event!!)
                    val latLng = LatLng(event!!.getLatitude(), event!!.getLongitude())
                    markerOptions = MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    marker = mMap.addMarker(markerOptions)
                    marker!!.tag = event.getEventID()
                }

                userRef.addValueEventListener(object: ValueEventListener{
                    override fun onDataChange(snapshotUser: DataSnapshot) {
                        (user as ArrayList<Users>).clear()
                        for (dataSnapshotUser in snapshotUser.children) {
                            val users = dataSnapshotUser.getValue(Users::class.java)
                            (user as ArrayList<Users>).add(users!!)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {

                    }

                })
                mMap.setInfoWindowAdapter(InfoWindowAdapter(requireContext(), mEvents, user))
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener{
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    mMap.isMyLocationEnabled = true
                    val mapView = mapFragment.view
                    val locationButton = (mapView!!.findViewById<View>("1".toInt()).parent as View).findViewById<View>("2".toInt())
                    val rlp = locationButton.layoutParams as (RelativeLayout.LayoutParams)
                    rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
                    rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                    rlp.setMargins(0, 0, 30, 30)
                    fusedLocationProviderClient.lastLocation
                        .addOnFailureListener{e ->
                            Log.e("Location", e.message.toString())
                        }
                        .addOnSuccessListener { location ->
                            val latLng = LatLng(location.latitude, location.longitude)
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
                        }
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(requireContext(), "Permission ${p0!!.permissionName} denied!", Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    p1!!.continuePermissionRequest()
                }
            }).check()
    }
}