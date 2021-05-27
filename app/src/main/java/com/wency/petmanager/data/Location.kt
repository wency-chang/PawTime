package com.wency.petmanager.data

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import kotlinx.android.parcel.Parcelize


data class Location(
    var locationName: String ,
    var locationAddress: String ,
    var locationLatlng : LatLng?
)
