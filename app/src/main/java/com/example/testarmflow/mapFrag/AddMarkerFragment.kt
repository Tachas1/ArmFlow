package com.example.testarmflow.mapFrag

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import com.example.testarmflow.R
import com.example.testarmflow.databinding.FragmentAddMarkerBinding
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
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.io.IOException


class AddMarkerFragment : Fragment(), OnMapReadyCallback {
    private lateinit var binding: FragmentAddMarkerBinding
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var marker: Marker? = null
    private lateinit var markerOptions: MarkerOptions

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentAddMarkerBinding.inflate(inflater, container, false)

        mapFragment = childFragmentManager.findFragmentById(R.id.mapAdd) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.apply {
            mapMenuAdd.setOnClickListener {
                binding.optionsLayoutAdd.visibility = View.VISIBLE
            }
            browseEvents.setOnClickListener{
                requireActivity().supportFragmentManager.popBackStack("fragments_map", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
            backToMap.setOnClickListener {
                binding.optionsLayoutAdd.visibility = View.GONE
            }
            nextStage.setOnClickListener {
                val latVal = binding.latitudeTxtAdd.text.toString().replace("Szerokosc : ", "")
                val longVal = binding.longitudeTxtAdd.text.toString().replace("Dlugosc : ", "")

                val bundle = Bundle()
                bundle.putString("latitude", latVal)
                bundle.putString("longitude", longVal)

                val fragment = CreateEventFragment()
                fragment.arguments = bundle
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(binding.root.id, fragment)
                    .setReorderingAllowed(true)
                    .addToBackStack("fragments_map")
                    .commit()
            }
            myEvents.setOnClickListener {
                val fragment = MyEventsFragment()
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(binding.root.id, fragment)
                transaction.setReorderingAllowed(true)
                transaction.addToBackStack("fragments_map")
                transaction.commit()
            }
        }

        mapInitialize()

        return binding.root
    }
    private fun mapInitialize() {
        var locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(2000)
            .setMaxUpdateDelayMillis(100)
            .build()

        binding.searchLocationAdd.setOnEditorActionListener(object : TextView.OnEditorActionListener {
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
        val searchLocation: String = binding.searchLocationAdd.text.toString()

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
                        goToLatLng(latitude, longitude, 17F)
                        if(marker != null){
                            marker!!.remove()
                        }
                        markerOptions = MarkerOptions()
                        markerOptions.title(location)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            .position(LatLng(latitude, longitude)).draggable(true)
                        marker = mMap.addMarker(markerOptions)!!
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
                goToLatLng(latitude, longitude, 17F)
                if(marker != null){
                    marker!!.remove()
                }
                markerOptions = MarkerOptions()
                markerOptions.title(location)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .position(LatLng(latitude, longitude)).draggable(true)
                marker = mMap.addMarker(markerOptions)!!
            }
        }
    }

    private fun goToLatLng(latitude: Double, longitude: Double, fl: Float) {
        val latLng = LatLng(latitude, longitude)
        binding.latitudeTxtAdd.text = ("Szerokosc : " + latLng.latitude.toString())
        binding.longitudeTxtAdd.text = ("Dlugosc : " + latLng.longitude.toString())
        val update = CameraUpdateFactory.newLatLngZoom(latLng, fl)
        mMap.animateCamera(update)
    }


    override fun onMapReady(map: GoogleMap) {
        mMap = map
        mMap.setOnMapClickListener { point ->
            val location = point

            binding.latitudeTxtAdd.text = ("Szerokosc : " + location.latitude.toString())
            binding.longitudeTxtAdd.text = ("Dlugosc : " + location.longitude.toString())

            if (binding.nextStage.visibility == View.GONE){
                binding.nextStage.visibility = View.VISIBLE
            }
        }

        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
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
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17F))
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

        if(mMap != null){
            mMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
                override fun onMarkerDrag(p0: Marker) {

                }

                override fun onMarkerDragEnd(p0: Marker) {
                    val geocoder = Geocoder(requireContext())
                    var list: MutableList<Address> = mutableListOf()
                    val markerPosition = p0.position
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(markerPosition.latitude, markerPosition.longitude, 1, object : Geocoder.GeocodeListener{
                            override fun onGeocode(p0: MutableList<Address>) {
                                val address = p0[0]
                                binding.latitudeTxtAdd.text = markerPosition.latitude.toString()
                                binding.longitudeTxtAdd.text = markerPosition.longitude.toString()
                                marker!!.title = address.adminArea
                            }
                        })
                    } else {
                        try {
                            list = geocoder.getFromLocation(markerPosition.latitude, markerPosition.longitude, 1) as MutableList<Address>
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        val address = list[0]
                        binding.latitudeTxtAdd.text = markerPosition.latitude.toString()
                        binding.longitudeTxtAdd.text = markerPosition.longitude.toString()
                        marker!!.title = address.adminArea
                    }
                }

                override fun onMarkerDragStart(p0: Marker) {

                }
            })
        }
    }

}